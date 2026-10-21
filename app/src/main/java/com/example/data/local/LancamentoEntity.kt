package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lancamentos")
data class LancamentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carteiraId: Long? = null, // ID da carteira vinculada
    val carteiraNome: String = "Binance", // Nome da carteira / corretora
    val tipoOperacao: String = "COMPRA", // "COMPRA", "VENDA", "PERMUTA", "TRANSFERENCIA", "RENDIMENTO", "DEPOSITO", "SAQUE", "AIRDROP"
    val dataHora: String = "", // "dd/MM/yyyy HH:mm"
    val timestamp: Long = System.currentTimeMillis(),
    val ativoPrincipal: String = "BTC", // Símbolo: "BTC", "ETH", "SOL"
    val nomeAtivo: String = "Bitcoin", // Nome da moeda
    val quantidade: Double = 0.0,
    val precoUnitario: Double = 0.0, // Preço unitário em BRL
    val valorTotal: Double = 0.0, // Valor total em BRL (quantidade * precoUnitario)
    val moedaCotacao: String = "BRL", // "BRL", "USDT", "USD"
    val taxa: Double = 0.0, // Quantidade da taxa cobrada
    val moedaTaxa: String = "BRL", // Moeda da taxa (BRL, BNB, ETH)
    val valorTaxaBrl: Double = 0.0, // Valor da taxa convertido em BRL (dedutível no IRPF)
    val ativoSecundario: String? = null, // Para Permuta/Swap: ativo recebido (ex: "ETH")
    val quantidadeSecundaria: Double? = null, // Para Permuta/Swap: quantidade recebida
    val precoUnitarioSecundario: Double? = null, // Cotação do ativo recebido em BRL
    val carteiraDestinoId: Long? = null, // Para Transferência entre carteiras
    val carteiraDestinoNome: String? = null,
    val txHash: String? = null, // Hash blockchain ou ID da ordem na exchange
    val observacoes: String? = null
) {
    fun toTransactionEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            timestamp = timestamp,
            dateFormatted = dataHora,
            exchange = carteiraNome,
            type = tipoOperacao,
            coinSymbol = ativoPrincipal,
            coinName = nomeAtivo,
            amount = quantidade,
            unitPriceBrl = precoUnitario,
            feeBrl = valorTaxaBrl,
            totalBrl = valorTotal,
            targetCoinSymbol = ativoSecundario,
            targetAmount = quantidadeSecundaria,
            notes = observacoes
        )
    }

    companion object {
        fun fromTransactionEntity(tx: TransactionEntity, carteiraId: Long? = null): LancamentoEntity {
            return LancamentoEntity(
                id = tx.id,
                carteiraId = carteiraId,
                carteiraNome = tx.exchange,
                tipoOperacao = tx.type,
                dataHora = tx.dateFormatted,
                timestamp = tx.timestamp,
                ativoPrincipal = tx.coinSymbol,
                nomeAtivo = tx.coinName,
                quantidade = tx.amount,
                precoUnitario = tx.unitPriceBrl,
                valorTotal = tx.totalBrl,
                moedaCotacao = "BRL",
                taxa = if (tx.feeBrl > 0) tx.feeBrl else 0.0,
                moedaTaxa = "BRL",
                valorTaxaBrl = tx.feeBrl,
                ativoSecundario = tx.targetCoinSymbol,
                quantidadeSecundaria = tx.targetAmount,
                observacoes = tx.notes
            )
        }
    }
}
