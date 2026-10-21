package com.example.data.backup

import com.example.data.local.CarteiraEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.LancamentoEntity
import com.example.data.local.PortfolioEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FullBackupData(
    val carteiras: List<CarteiraEntity> = emptyList(),
    val lancamentos: List<LancamentoEntity> = emptyList(),
    val portfolio: List<PortfolioEntity> = emptyList(),
    val favorites: List<FavoriteEntity> = emptyList()
)

data class BackupImportResult(
    val success: Boolean,
    val importedHoldingsCount: Int,
    val importedFavoritesCount: Int,
    val importedCarteirasCount: Int = 0,
    val importedLancamentosCount: Int = 0,
    val message: String
)

object PortfolioBackupManager {

    /**
     * Serializes complete CriptoFull data: carteiras, lancamentos, portfolio and favorites into JSON.
     */
    fun createFullBackupJson(
        carteiras: List<CarteiraEntity>,
        lancamentos: List<LancamentoEntity>,
        portfolio: List<PortfolioEntity>,
        favorites: List<FavoriteEntity>
    ): String {
        val root = JSONObject()
        root.put("app", "CriptoFull")
        root.put("version", "2.0")
        root.put("exportDate", System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        root.put("exportDateFormatted", dateFormat.format(Date()))

        // 1. Carteiras array
        val carteirasArray = JSONArray()
        for (c in carteiras) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("nome", c.nome)
            obj.put("tipo", c.tipo)
            obj.put("corretora", c.corretora)
            obj.put("endereco", c.endereco ?: "")
            obj.put("cor", c.cor)
            obj.put("observacoes", c.observacoes ?: "")
            obj.put("criadoEm", c.criadoEm)
            obj.put("ativo", c.ativo)
            carteirasArray.put(obj)
        }
        root.put("carteiras", carteirasArray)

        // 2. Lançamentos array
        val lancamentosArray = JSONArray()
        for (l in lancamentos) {
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("carteiraId", l.carteiraId ?: 0)
            obj.put("carteiraNome", l.carteiraNome)
            obj.put("tipoOperacao", l.tipoOperacao)
            obj.put("dataHora", l.dataHora)
            obj.put("timestamp", l.timestamp)
            obj.put("ativoPrincipal", l.ativoPrincipal)
            obj.put("nomeAtivo", l.nomeAtivo)
            obj.put("quantidade", l.quantidade)
            obj.put("precoUnitario", l.precoUnitario)
            obj.put("valorTotal", l.valorTotal)
            obj.put("moedaCotacao", l.moedaCotacao)
            obj.put("taxa", l.taxa)
            obj.put("moedaTaxa", l.moedaTaxa)
            obj.put("valorTaxaBrl", l.valorTaxaBrl)
            obj.put("ativoSecundario", l.ativoSecundario ?: "")
            obj.put("quantidadeSecundaria", l.quantidadeSecundaria ?: 0.0)
            obj.put("carteiraDestinoNome", l.carteiraDestinoNome ?: "")
            obj.put("txHash", l.txHash ?: "")
            obj.put("observacoes", l.observacoes ?: "")
            lancamentosArray.put(obj)
        }
        root.put("lancamentos", lancamentosArray)

        // 3. Portfolio array
        val portfolioArray = JSONArray()
        for (item in portfolio) {
            val itemObj = JSONObject()
            itemObj.put("coinId", item.coinId)
            itemObj.put("coinSymbol", item.coinSymbol)
            itemObj.put("coinName", item.coinName)
            itemObj.put("coinImage", item.coinImage ?: "")
            itemObj.put("amount", item.amount)
            itemObj.put("buyPriceUsd", item.buyPriceUsd)
            itemObj.put("timestamp", item.timestamp)
            portfolioArray.put(itemObj)
        }
        root.put("portfolio", portfolioArray)

        // 4. Favorites array
        val favArray = JSONArray()
        for (fav in favorites) {
            favArray.put(fav.coinId)
        }
        root.put("favorites", favArray)

        return root.toString(2)
    }

    /**
     * Parses full backup data from CriptoFull JSON or legacy formats.
     */
    fun parseFullBackupData(rawContent: String): FullBackupData {
        val trimmed = rawContent.trim()
        val carteiras = mutableListOf<CarteiraEntity>()
        val lancamentos = mutableListOf<LancamentoEntity>()
        val holdings = mutableListOf<PortfolioEntity>()
        val favorites = mutableListOf<FavoriteEntity>()

        if (trimmed.startsWith("{")) {
            val jsonObject = JSONObject(trimmed)

            // Carteiras
            val carteirasArray = jsonObject.optJSONArray("carteiras") ?: jsonObject.optJSONArray("wallets")
            if (carteirasArray != null) {
                for (i in 0 until carteirasArray.length()) {
                    val cObj = carteirasArray.optJSONObject(i) ?: continue
                    carteiras.add(
                        CarteiraEntity(
                            nome = cObj.optString("nome", cObj.optString("name", "Carteira $i")),
                            tipo = cObj.optString("tipo", cObj.optString("type", "EXCHANGE")),
                            corretora = cObj.optString("corretora", cObj.optString("exchange", "Binance")),
                            endereco = cObj.optString("endereco", cObj.optString("address", null))?.ifBlank { null },
                            cor = cObj.optString("cor", cObj.optString("color", "#00E5FF")),
                            observacoes = cObj.optString("observacoes", cObj.optString("observacao", cObj.optString("notes", null)))?.ifBlank { null },
                            criadoEm = cObj.optLong("criadoEm", cObj.optLong("criado_em", cObj.optLong("created_at", System.currentTimeMillis()))),
                            ativo = cObj.optBoolean("ativo", cObj.optBoolean("active", true))
                        )
                    )
                }
            }

            // Lançamentos
            val lancamentosArray = jsonObject.optJSONArray("lancamentos")
                ?: jsonObject.optJSONArray("transactions")
                ?: jsonObject.optJSONArray("operacoes")
            if (lancamentosArray != null) {
                for (i in 0 until lancamentosArray.length()) {
                    val lObj = lancamentosArray.optJSONObject(i) ?: continue
                    val symbol = lObj.optString("ativoPrincipal", lObj.optString("ativo_principal", lObj.optString("coinSymbol", lObj.optString("symbol", "BTC")))).uppercase()
                    val amount = lObj.optDouble("quantidade", lObj.optDouble("amount", lObj.optDouble("qtd", 0.0)))
                    val price = lObj.optDouble("precoUnitario", lObj.optDouble("preco_unitario", lObj.optDouble("unitPriceBrl", lObj.optDouble("preco", 0.0))))
                    val total = lObj.optDouble("valorTotal", lObj.optDouble("valor_total", lObj.optDouble("totalBrl", lObj.optDouble("total", amount * price))))

                    lancamentos.add(
                        LancamentoEntity(
                            carteiraNome = lObj.optString("carteiraNome", lObj.optString("carteira_nome", lObj.optString("exchange", lObj.optString("corretora", "Binance")))),
                            tipoOperacao = lObj.optString("tipoOperacao", lObj.optString("tipo_operacao", lObj.optString("type", lObj.optString("tipo", "COMPRA")))).uppercase(),
                            dataHora = lObj.optString("dataHora", lObj.optString("data_hora", lObj.optString("dateFormatted", lObj.optString("data", "")))),
                            timestamp = lObj.optLong("timestamp", System.currentTimeMillis()),
                            ativoPrincipal = symbol,
                            nomeAtivo = lObj.optString("nomeAtivo", lObj.optString("nome_ativo", lObj.optString("coinName", symbol))),
                            quantidade = amount,
                            precoUnitario = if (price > 0) price else if (amount > 0) total / amount else 0.0,
                            valorTotal = total,
                            moedaCotacao = lObj.optString("moedaCotacao", lObj.optString("moeda_cotacao", "BRL")),
                            taxa = lObj.optDouble("taxa", lObj.optDouble("fee", 0.0)),
                            moedaTaxa = lObj.optString("moedaTaxa", lObj.optString("moeda_taxa", "BRL")),
                            valorTaxaBrl = lObj.optDouble("valorTaxaBrl", lObj.optDouble("valor_taxa_brl", lObj.optDouble("feeBrl", 0.0))),
                            ativoSecundario = lObj.optString("ativoSecundario", lObj.optString("ativo_secundario", null))?.ifBlank { null },
                            quantidadeSecundaria = if (lObj.has("quantidadeSecundaria")) lObj.optDouble("quantidadeSecundaria") else if (lObj.has("quantidade_secundaria")) lObj.optDouble("quantidade_secundaria") else null,
                            precoUnitarioSecundario = if (lObj.has("precoUnitarioSecundario")) lObj.optDouble("precoUnitarioSecundario") else if (lObj.has("preco_unitario_secundario")) lObj.optDouble("preco_unitario_secundario") else null,
                            carteiraDestinoNome = lObj.optString("carteiraDestinoNome", lObj.optString("carteira_destino_nome", null))?.ifBlank { null },
                            txHash = lObj.optString("txHash", lObj.optString("tx_hash", null))?.ifBlank { null },
                            observacoes = lObj.optString("observacoes", lObj.optString("observacao", lObj.optString("notes", null)))?.ifBlank { null }
                        )
                    )
                }
            }

            // Portfolio / Holdings
            val portfolioArray = jsonObject.optJSONArray("portfolio")
                ?: jsonObject.optJSONArray("holdings")
                ?: jsonObject.optJSONArray("items")
                ?: jsonObject.optJSONArray("carteira")
            if (portfolioArray != null) {
                parsePortfolioJsonArray(portfolioArray, holdings)
            }

            // Favorites
            val favoritesArray = jsonObject.optJSONArray("favorites")
                ?: jsonObject.optJSONArray("favoritos")
            if (favoritesArray != null) {
                for (i in 0 until favoritesArray.length()) {
                    val favObj = favoritesArray.opt(i)
                    when (favObj) {
                        is String -> if (favObj.isNotBlank()) favorites.add(FavoriteEntity(favObj))
                        is JSONObject -> {
                            val id = favObj.optString("coinId", favObj.optString("id"))
                            if (id.isNotBlank()) favorites.add(FavoriteEntity(id))
                        }
                    }
                }
            }
        } else if (trimmed.startsWith("[")) {
            val jsonArray = JSONArray(trimmed)
            parsePortfolioJsonArray(jsonArray, holdings)
        } else {
            // CSV lines
            val lines = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.size > 1) {
                val header = lines[0].split(",").map { it.trim().lowercase() }
                val coinIdIdx = header.indexOfFirst { it.contains("coin") || it.contains("id") }
                val symbolIdx = header.indexOfFirst { it.contains("symbol") || it.contains("ticker") }
                val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("nome") }
                val amountIdx = header.indexOfFirst { it.contains("amount") || it.contains("qtd") || it.contains("quantidade") }
                val priceIdx = header.indexOfFirst { it.contains("price") || it.contains("preco") }

                for (i in 1 until lines.size) {
                    val parts = lines[i].split(",").map { it.trim() }
                    val coinId = if (coinIdIdx >= 0 && coinIdIdx < parts.size) parts[coinIdIdx] else continue
                    val symbol = if (symbolIdx >= 0 && symbolIdx < parts.size) parts[symbolIdx] else coinId.take(4).uppercase()
                    val name = if (nameIdx >= 0 && nameIdx < parts.size) parts[nameIdx] else symbol
                    val amount = if (amountIdx >= 0 && amountIdx < parts.size) parts[amountIdx].toDoubleOrNull() ?: 0.0 else 0.0
                    val price = if (priceIdx >= 0 && priceIdx < parts.size) parts[priceIdx].toDoubleOrNull() ?: 0.0 else 0.0

                    if (amount > 0) {
                        holdings.add(
                            PortfolioEntity(
                                coinId = coinId,
                                coinSymbol = symbol,
                                coinName = name,
                                amount = amount,
                                buyPriceUsd = price
                            )
                        )
                    }
                }
            }
        }

        return FullBackupData(
            carteiras = carteiras,
            lancamentos = lancamentos,
            portfolio = holdings,
            favorites = favorites
        )
    }

    fun parseBackupData(rawContent: String): Pair<List<PortfolioEntity>, List<FavoriteEntity>> {
        val full = parseFullBackupData(rawContent)
        return Pair(full.portfolio, full.favorites)
    }

    fun createBackupJson(holdings: List<PortfolioEntity>, favorites: List<FavoriteEntity>): String {
        return createFullBackupJson(emptyList(), emptyList(), holdings, favorites)
    }

    private fun parsePortfolioJsonArray(array: JSONArray, result: MutableList<PortfolioEntity>) {
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val coinId = obj.optString("coinId", obj.optString("id", "")).trim()
            if (coinId.isEmpty()) continue

            val symbol = obj.optString("coinSymbol", obj.optString("symbol", coinId.take(4))).uppercase()
            val name = obj.optString("coinName", obj.optString("name", symbol))
            val image = obj.optString("coinImage", obj.optString("image", ""))
            val amount = obj.optDouble("amount", obj.optDouble("quantity", 0.0))
            val buyPriceUsd = obj.optDouble("buyPriceUsd", obj.optDouble("price", obj.optDouble("buyPrice", 0.0)))
            val timestamp = obj.optLong("timestamp", System.currentTimeMillis())

            if (amount > 0) {
                result.add(
                    PortfolioEntity(
                        coinId = coinId,
                        coinSymbol = symbol,
                        coinName = name,
                        coinImage = if (image.isNotBlank()) image else null,
                        amount = amount,
                        buyPriceUsd = buyPriceUsd,
                        timestamp = timestamp
                    )
                )
            }
        }
    }
}
