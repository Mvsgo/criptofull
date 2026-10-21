package com.example.model

enum class TransactionType(val label: String, val isSale: Boolean, val isBuy: Boolean) {
    COMPRA("Compra", false, true),
    VENDA("Venda", true, false),
    PERMUTA("Permuta (Troca)", true, true),
    TRANSFERENCIA("Transferência / Depósito / Saque", false, false),
    RENDIMENTO("Rendimento / Staking / Airdrop", false, true);

    companion object {
        fun fromString(value: String): TransactionType {
            return when (value.uppercase().trim()) {
                "COMPRA", "BUY" -> COMPRA
                "VENDA", "SELL" -> VENDA
                "PERMUTA", "SWAP", "TRADE" -> PERMUTA
                "TRANSFERENCIA", "TRANSFER", "DEPOSIT", "WITHDRAW" -> TRANSFERENCIA
                "RENDIMENTO", "STAKING", "AIRDROP", "MINING" -> RENDIMENTO
                else -> COMPRA
            }
        }
    }
}

enum class IrpfCategory(val code: String, val group: String, val description: String) {
    BITCOIN("01", "08", "Criptoativo Bitcoin (BTC)"),
    OUTRAS_CRIPTOS("02", "08", "Outras Criptomoedas (ETH, SOL, ADA, etc.)"),
    STABLECOINS("03", "08", "Criptoativos referenciados em moedas fiat / Stablecoins (USDT, USDC, etc.)"),
    NFTS("10", "08", "Tokens Não Fungíveis (NFTs)"),
    OUTROS("99", "08", "Outros Criptoativos");

    companion object {
        fun detectCategory(symbol: String): IrpfCategory {
            val s = symbol.uppercase().trim()
            return when {
                s == "BTC" || s == "BITCOIN" -> BITCOIN
                s in listOf("USDT", "USDC", "DAI", "BUSD", "BRZ", "FDUSD", "TUSD") -> STABLECOINS
                s in listOf("ETH", "SOL", "BNB", "ADA", "XRP", "DOGE", "AVAX", "DOT", "LINK", "MATIC", "NEAR", "SUI") -> OUTRAS_CRIPTOS
                else -> OUTRAS_CRIPTOS
            }
        }
    }
}

data class SaleCalculationDetail(
    val transactionId: Long,
    val dateString: String,
    val timestamp: Long,
    val coinSymbol: String,
    val amountSold: Double,
    val saleGrossValueBrl: Double,
    val feeBrl: Double,
    val saleNetValueBrl: Double,
    val averageCostPriceBrl: Double,
    val acquisitionCostBrl: Double,
    val profitOrLossBrl: Double,
    val isProfit: Boolean
)

data class MonthlyTaxReport(
    val year: Int,
    val month: Int,
    val totalSalesBrl: Double = 0.0,
    val isExempt: Boolean = true, // Sales <= R$ 35.000,00
    val grossProfitBrl: Double = 0.0,
    val grossLossBrl: Double = 0.0,
    val exemptProfitBrl: Double = 0.0,
    val taxableProfitBrl: Double = 0.0,
    val previousAccumulatedLossBrl: Double = 0.0,
    val lossCompensatedBrl: Double = 0.0,
    val netTaxableBaseBrl: Double = 0.0,
    val taxDueBrl: Double = 0.0, // 15% on net taxable base
    val newAccumulatedLossBrl: Double = 0.0,
    val darfDueDate: String = "",
    val sales: List<SaleCalculationDetail> = emptyList()
) {
    val monthFormatted: String
        get() = String.format("%02d/%04d", month, year)
}

data class IrpfAssetDeclaration(
    val coinSymbol: String,
    val coinName: String,
    val irpfCategory: IrpfCategory,
    val quantity: Double,
    val averagePriceBrl: Double,
    val totalAcquisitionCostBrl: Double,
    val exchangeName: String,
    val formattedDiscrimination: String
)

data class AnnualIrpfReport(
    val year: Int,
    val totalExemptProfitsYearBrl: Double,
    val totalTaxableProfitsYearBrl: Double,
    val totalTaxesDueYearBrl: Double,
    val assetsPositionDecember31: List<IrpfAssetDeclaration>,
    val monthlyReports: List<MonthlyTaxReport>
)
