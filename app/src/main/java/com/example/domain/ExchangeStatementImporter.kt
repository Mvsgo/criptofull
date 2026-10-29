package com.example.domain

import com.example.data.local.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ImportStatementResult(
    val success: Boolean,
    val exchangeName: String,
    val importedCount: Int,
    val transactions: List<TransactionEntity>,
    val message: String
)

class ExchangeStatementImporter {

    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR")),
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")),
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    )

    fun parseStatement(rawContent: String, defaultExchange: String = "Binance"): ImportStatementResult {
        val trimmed = rawContent.trim()
        if (trimmed.isEmpty()) {
            return ImportStatementResult(false, defaultExchange, 0, emptyList(), "O extrato fornecido está vazio.")
        }

        return try {
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                parseJsonStatement(trimmed, defaultExchange)
            } else {
                parseCsvStatement(trimmed, defaultExchange)
            }
        } catch (e: Exception) {
            ImportStatementResult(false, defaultExchange, 0, emptyList(), "Erro ao processar extrato: ${e.message}")
        }
    }

    private fun parseJsonStatement(jsonStr: String, defaultExchange: String): ImportStatementResult {
        val list = mutableListOf<TransactionEntity>()
        val outputDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

        if (jsonStr.startsWith("{")) {
            val root = JSONObject(jsonStr)
            val txArray = root.optJSONArray("lancamentos")
                ?: root.optJSONArray("transactions")
                ?: root.optJSONArray("operacoes")
                ?: root.optJSONArray("extrato")
                ?: root.optJSONArray("trades")
                ?: root.optJSONArray("portfolio")

            if (txArray != null) {
                parseJsonArray(txArray, list, defaultExchange, outputDateFormat)
            }
        } else {
            val array = JSONArray(jsonStr)
            parseJsonArray(array, list, defaultExchange, outputDateFormat)
        }

        return ImportStatementResult(
            success = list.isNotEmpty(),
            exchangeName = defaultExchange,
            importedCount = list.size,
            transactions = list,
            message = if (list.isNotEmpty()) "${list.size} operações importadas com sucesso!" else "Nenhuma operação válida encontrada no arquivo JSON."
        )
    }

    private fun parseJsonArray(
        array: JSONArray,
        targetList: MutableList<TransactionEntity>,
        defaultExchange: String,
        outputDateFormat: SimpleDateFormat
    ) {
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val symbol = obj.optString("ativoPrincipal", obj.optString("ativo_principal", obj.optString("coinSymbol", obj.optString("symbol", obj.optString("cripto", "BTC"))))).uppercase()
            val typeStr = obj.optString("tipoOperacao", obj.optString("tipo_operacao", obj.optString("type", obj.optString("tipo", "COMPRA")))).uppercase()
            val amount = obj.optDouble("quantidade", obj.optDouble("amount", obj.optDouble("qtd", 0.0)))
            val price = obj.optDouble("precoUnitario", obj.optDouble("preco_unitario", obj.optDouble("unitPriceBrl", obj.optDouble("preco", obj.optDouble("price", obj.optDouble("buyPriceUsd", 0.0))))))
            val fee = obj.optDouble("valorTaxaBrl", obj.optDouble("valor_taxa_brl", obj.optDouble("taxa", obj.optDouble("feeBrl", obj.optDouble("fee", 0.0)))))
            var total = obj.optDouble("valorTotal", obj.optDouble("valor_total", obj.optDouble("totalBrl", obj.optDouble("total", amount * price))))
            if (total <= 0.0) total = amount * price

            val exchange = obj.optString("carteiraNome", obj.optString("carteira_nome", obj.optString("exchange", obj.optString("corretora", defaultExchange))))
            val dateStrRaw = obj.optString("dataHora", obj.optString("data_hora", obj.optString("date", obj.optString("data", ""))))
            val timestamp = parseDateToTimestamp(dateStrRaw)

            if (amount > 0.0) {
                targetList.add(
                    TransactionEntity(
                        timestamp = timestamp,
                        dateFormatted = outputDateFormat.format(Date(timestamp)),
                        exchange = exchange,
                        type = typeStr,
                        coinSymbol = symbol,
                        coinName = obj.optString("nomeAtivo", obj.optString("nome_ativo", getCoinName(symbol))),
                        amount = amount,
                        unitPriceBrl = if (price > 0) price else total / amount,
                        feeBrl = fee,
                        totalBrl = total,
                        notes = obj.optString("observacoes", obj.optString("observacao", obj.optString("notes", null)))
                    )
                )
            }
        }
    }

    private fun parseCsvStatement(csv: String, defaultExchange: String): ImportStatementResult {
        val lines = csv.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size < 2) {
            return ImportStatementResult(false, defaultExchange, 0, emptyList(), "Arquivo CSV precisa conter cabeçalho e pelo menos 1 linha de dados.")
        }

        val delimiter = if (lines[0].contains(";")) ";" else ","
        val header = lines[0].split(delimiter).map { it.trim().lowercase().replace("\"", "").replace("'", "") }

        // Detect exchange by header signatures
        var detectedExchange = defaultExchange
        if (header.any { it.contains("utc_time") || it.contains("date(utc)") || it.contains("market") }) {
            detectedExchange = "Binance"
        } else if (header.any { it.contains("instrumento") || it.contains("tipo da ordem") || it.contains("total bruto") }) {
            detectedExchange = "Mercado Bitcoin"
        } else if (header.any { it.contains("order no") || it.contains("spot") || it.contains("filled") }) {
            detectedExchange = "Bybit"
        }

        val list = mutableListOf<TransactionEntity>()
        val outputDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

        for (i in 1 until lines.size) {
            val parts = lines[i].split(delimiter).map { it.trim().replace("\"", "").replace("'", "") }
            if (parts.size < 3) continue

            val tx = parseCsvLine(parts, header, detectedExchange, outputDateFormat)
            if (tx != null) {
                list.add(tx)
            }
        }

        return ImportStatementResult(
            success = list.isNotEmpty(),
            exchangeName = detectedExchange,
            importedCount = list.size,
            transactions = list,
            message = if (list.isNotEmpty()) "${list.size} operações importadas da $detectedExchange!" else "Não foi possível extrair operações do arquivo CSV."
        )
    }

    private fun parseCsvLine(
        parts: List<String>,
        header: List<String>,
        exchange: String,
        outputDateFormat: SimpleDateFormat
    ): TransactionEntity? {
        var dateRaw = ""
        var typeRaw = "COMPRA"
        var symbolRaw = ""
        var amount = 0.0
        var unitPrice = 0.0
        var total = 0.0
        var fee = 0.0

        for (idx in parts.indices) {
            if (idx >= header.size) break
            val col = header[idx]
            val value = parts[idx]

            when {
                col.contains("date") || col.contains("data") || col.contains("time") -> {
                    if (dateRaw.isEmpty()) dateRaw = value
                }
                col.contains("type") || col.contains("tipo") || col.contains("side") || col.contains("ordem") -> {
                    typeRaw = value
                }
                col.contains("pair") || col.contains("market") || col.contains("instrumento") || col.contains("symbol") || col.contains("moeda") || col.contains("cripto") || col.contains("coin") -> {
                    if (symbolRaw.isEmpty()) symbolRaw = value
                }
                col.contains("amount") || col.contains("qtd") || col.contains("quantidade") || col.contains("volume") || col.contains("executado") -> {
                    if (amount == 0.0) amount = cleanDouble(value)
                }
                col.contains("price") || col.contains("preço") || col.contains("unitário") || col.contains("cotação") -> {
                    if (unitPrice == 0.0) unitPrice = cleanDouble(value)
                }
                col.contains("total") || col.contains("valor") || col.contains("financeiro") -> {
                    if (total == 0.0) total = cleanDouble(value)
                }
                col.contains("fee") || col.contains("taxa") || col.contains("comissão") -> {
                    if (fee == 0.0) fee = cleanDouble(value)
                }
            }
        }

        // Clean symbol (e.g., BTCBRL -> BTC, ETHUSDT -> ETH, ETHBTC -> ETH, SOLBNB -> SOL)
        var cleanSymbol = symbolRaw.uppercase().replace("/", "").replace("_", "").replace("-", "")
        if (cleanSymbol.endsWith("BRL")) {
            cleanSymbol = cleanSymbol.removeSuffix("BRL")
        } else if (cleanSymbol.endsWith("USDT")) {
            cleanSymbol = cleanSymbol.removeSuffix("USDT")
        } else if (cleanSymbol.endsWith("USDC")) {
            cleanSymbol = cleanSymbol.removeSuffix("USDC")
        } else if (cleanSymbol.endsWith("USD")) {
            cleanSymbol = cleanSymbol.removeSuffix("USD")
        } else if (cleanSymbol.endsWith("BTC") && cleanSymbol.length > 3) {
            cleanSymbol = cleanSymbol.removeSuffix("BTC")
        } else if (cleanSymbol.endsWith("BNB") && cleanSymbol.length > 3) {
            cleanSymbol = cleanSymbol.removeSuffix("BNB")
        } else if (cleanSymbol.endsWith("ETH") && cleanSymbol.length > 3) {
            cleanSymbol = cleanSymbol.removeSuffix("ETH")
        }
        if (cleanSymbol.isBlank()) cleanSymbol = "BTC"

        // Normalize transaction type
        val normType = when {
            typeRaw.uppercase().contains("SELL") || typeRaw.uppercase().contains("VENDA") -> "VENDA"
            typeRaw.uppercase().contains("SWAP") || typeRaw.uppercase().contains("PERMUTA") || typeRaw.uppercase().contains("CONVERT") -> "PERMUTA"
            typeRaw.uppercase().contains("WITHDRAW") || typeRaw.uppercase().contains("SAQUE") -> "TRANSFERENCIA"
            typeRaw.uppercase().contains("DEPOSIT") || typeRaw.uppercase().contains("DEPÓSITO") -> "TRANSFERENCIA"
            typeRaw.uppercase().contains("STAKING") || typeRaw.uppercase().contains("AIRDROP") -> "RENDIMENTO"
            else -> "COMPRA"
        }

        if (total <= 0.0 && amount > 0.0 && unitPrice > 0.0) {
            total = amount * unitPrice
        }
        if (unitPrice <= 0.0 && amount > 0.0 && total > 0.0) {
            unitPrice = total / amount
        }

        if (amount <= 0.0 || total <= 0.0) return null

        val timestamp = parseDateToTimestamp(dateRaw)

        return TransactionEntity(
            timestamp = timestamp,
            dateFormatted = outputDateFormat.format(Date(timestamp)),
            exchange = exchange,
            type = normType,
            coinSymbol = cleanSymbol,
            coinName = getCoinName(cleanSymbol),
            amount = amount,
            unitPriceBrl = unitPrice,
            feeBrl = fee,
            totalBrl = total
        )
    }

    private fun cleanDouble(str: String): Double {
        var s = str.trim()
        if (s.contains(",") && s.contains(".")) {
            // e.g. 1.250,50 -> 1250.50
            if (s.lastIndexOf(',') > s.lastIndexOf('.')) {
                s = s.replace(".", "").replace(",", ".")
            } else {
                s = s.replace(",", "")
            }
        } else if (s.contains(",")) {
            s = s.replace(",", ".")
        }
        // remove any non-numeric except dot and minus
        s = s.replace(Regex("[^0-9.-]"), "")
        return s.toDoubleOrNull() ?: 0.0
    }

    private fun parseDateToTimestamp(raw: String): Long {
        val trimmed = raw.trim()
        for (format in dateFormats) {
            try {
                val d = format.parse(trimmed)
                if (d != null) return d.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    private fun getCoinName(symbol: String): String {
        return when (symbol.uppercase()) {
            "BTC" -> "Bitcoin"
            "ETH" -> "Ethereum"
            "SOL" -> "Solana"
            "BNB" -> "BNB"
            "ADA" -> "Cardano"
            "XRP" -> "XRP"
            "DOGE" -> "Dogecoin"
            "USDT" -> "Tether USD"
            "USDC" -> "USD Coin"
            "AVAX" -> "Avalanche"
            "DOT" -> "Polkadot"
            "LINK" -> "Chainlink"
            "NEAR" -> "NEAR Protocol"
            "SUI" -> "Sui"
            "MATIC" -> "Polygon"
            else -> symbol.uppercase()
        }
    }
}
