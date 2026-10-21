package com.example.domain

import com.example.data.local.TransactionEntity
import com.example.model.AnnualIrpfReport
import com.example.model.IrpfAssetDeclaration
import com.example.model.IrpfCategory
import com.example.model.MonthlyTaxReport
import com.example.model.SaleCalculationDetail
import com.example.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CryptoTaxEngine {

    data class AssetInventory(
        var quantity: Double = 0.0,
        var totalCostBrl: Double = 0.0,
        var exchange: String = "Binance"
    ) {
        val averagePriceBrl: Double
            get() = if (quantity > 0.00000001) totalCostBrl / quantity else 0.0
    }

    /**
     * Calculates complete monthly tax reports with:
     * - R$ 35.000,00 monthly sales exemption rule
     * - Cumulative weighted average cost (Custo Médio Ponderado)
     * - Loss compensation between taxable months
     * - 15% DARF capital gains tax calculation
     */
    fun processTransactions(transactions: List<TransactionEntity>): List<MonthlyTaxReport> {
        val sortedTransactions = transactions.sortedBy { it.timestamp }
        val inventoryMap = mutableMapOf<String, AssetInventory>() // Symbol -> Inventory

        // Group operations by Year and Month
        val calendar = Calendar.getInstance()
        val monthlyTransactions = sortedTransactions.groupBy { tx ->
            calendar.timeInMillis = tx.timestamp
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 1-12
            Pair(year, month)
        }

        var accumulatedLossBrl = 0.0
        val monthlyReports = mutableListOf<MonthlyTaxReport>()

        for ((yearMonth, txList) in monthlyTransactions) {
            val (year, month) = yearMonth
            var monthlySalesTotalBrl = 0.0
            val salesDetails = mutableListOf<SaleCalculationDetail>()

            // Process each transaction chronologically in this month
            for (tx in txList) {
                val symbol = tx.coinSymbol.uppercase()
                val inventory = inventoryMap.getOrPut(symbol) { AssetInventory(exchange = tx.exchange) }
                inventory.exchange = tx.exchange

                val type = TransactionType.fromString(tx.type)

                when (type) {
                    TransactionType.COMPRA, TransactionType.RENDIMENTO -> {
                        // Buy / Staking: add quantity and cost to inventory
                        inventory.quantity += tx.amount
                        val costWithFee = tx.totalBrl + tx.feeBrl
                        inventory.totalCostBrl += costWithFee
                    }

                    TransactionType.VENDA, TransactionType.PERMUTA -> {
                        // Sale / Swap: alienates crypto
                        val amountToSell = tx.amount
                        val currentAvgPrice = inventory.averagePriceBrl
                        val currentQty = inventory.quantity

                        val effectiveSoldQty = if (amountToSell <= currentQty) amountToSell else currentQty.coerceAtLeast(0.0)
                        val acquisitionCost = effectiveSoldQty * currentAvgPrice
                        val netSaleProceeds = (tx.totalBrl - tx.feeBrl).coerceAtLeast(0.0)
                        val profitOrLoss = netSaleProceeds - acquisitionCost

                        // Track total sales value in this month
                        monthlySalesTotalBrl += tx.totalBrl

                        // Update inventory
                        inventory.quantity = (inventory.quantity - effectiveSoldQty).coerceAtLeast(0.0)
                        inventory.totalCostBrl = (inventory.totalCostBrl - acquisitionCost).coerceAtLeast(0.0)

                        salesDetails.add(
                            SaleCalculationDetail(
                                transactionId = tx.id,
                                dateString = tx.dateFormatted,
                                timestamp = tx.timestamp,
                                coinSymbol = symbol,
                                amountSold = tx.amount,
                                saleGrossValueBrl = tx.totalBrl,
                                feeBrl = tx.feeBrl,
                                saleNetValueBrl = netSaleProceeds,
                                averageCostPriceBrl = currentAvgPrice,
                                acquisitionCostBrl = acquisitionCost,
                                profitOrLossBrl = profitOrLoss,
                                isProfit = profitOrLoss >= 0.0
                            )
                        )

                        // If it's a swap (PERMUTA), also add the acquired target coin to inventory!
                        if (type == TransactionType.PERMUTA && !tx.targetCoinSymbol.isNullOrBlank() && (tx.targetAmount ?: 0.0) > 0.0) {
                            val targetSymbol = tx.targetCoinSymbol.uppercase()
                            val targetInv = inventoryMap.getOrPut(targetSymbol) { AssetInventory(exchange = tx.exchange) }
                            targetInv.quantity += (tx.targetAmount ?: 0.0)
                            targetInv.totalCostBrl += netSaleProceeds // Acquisition cost of acquired coin is market value of given coin
                        }
                    }

                    TransactionType.TRANSFERENCIA -> {
                        // Transfer between wallets: adjust fee from cost
                        if (tx.feeBrl > 0.0) {
                            inventory.totalCostBrl += tx.feeBrl
                        }
                    }
                }
            }

            // Apply Brazilian Tax Rules for this month
            // Rule 1: Exemption limit is R$ 35.000,00 total sales in the calendar month
            val isExempt = monthlySalesTotalBrl <= 35000.00

            var grossProfit = 0.0
            var grossLoss = 0.0
            for (sale in salesDetails) {
                if (sale.profitOrLossBrl >= 0.0) {
                    grossProfit += sale.profitOrLossBrl
                } else {
                    grossLoss += kotlin.math.abs(sale.profitOrLossBrl)
                }
            }

            var exemptProfit = 0.0
            var taxableProfit = 0.0
            var lossCompensated = 0.0
            var netTaxableBase = 0.0
            var taxDue = 0.0
            val previousLoss = accumulatedLossBrl

            if (isExempt) {
                // All profits are exempt!
                exemptProfit = grossProfit
                taxableProfit = 0.0
                // Losses in exempt months are not compensated against taxable months per RFB guidelines
            } else {
                // Taxable month! Net gain = grossProfit - grossLoss
                val monthlyNetResult = grossProfit - grossLoss

                if (monthlyNetResult > 0.0) {
                    taxableProfit = monthlyNetResult
                    // Compensate accumulated losses from previous taxable months
                    if (previousLoss > 0.0) {
                        if (taxableProfit >= previousLoss) {
                            lossCompensated = previousLoss
                            netTaxableBase = taxableProfit - previousLoss
                            accumulatedLossBrl = 0.0
                        } else {
                            lossCompensated = taxableProfit
                            netTaxableBase = 0.0
                            accumulatedLossBrl = previousLoss - taxableProfit
                        }
                    } else {
                        netTaxableBase = taxableProfit
                    }

                    // 15% DARF tax rate on capital gain
                    taxDue = netTaxableBase * 0.15
                } else {
                    // Net loss in a taxable month accumulates for future compensation
                    val monthlyLoss = kotlin.math.abs(monthlyNetResult)
                    accumulatedLossBrl += monthlyLoss
                }
            }

            // Calculate DARF due date: last business day of the following month
            val darfDueStr = calculateDarfDueDate(year, month)

            monthlyReports.add(
                MonthlyTaxReport(
                    year = year,
                    month = month,
                    totalSalesBrl = monthlySalesTotalBrl,
                    isExempt = isExempt,
                    grossProfitBrl = grossProfit,
                    grossLossBrl = grossLoss,
                    exemptProfitBrl = exemptProfit,
                    taxableProfitBrl = taxableProfit,
                    previousAccumulatedLossBrl = previousLoss,
                    lossCompensatedBrl = lossCompensated,
                    netTaxableBaseBrl = netTaxableBase,
                    taxDueBrl = taxDue,
                    newAccumulatedLossBrl = accumulatedLossBrl,
                    darfDueDate = darfDueStr,
                    sales = salesDetails
                )
            )
        }

        return monthlyReports.sortedWith(compareByDescending<MonthlyTaxReport> { it.year }.thenByDescending { it.month })
    }

    /**
     * Computes the current inventory positions and prepares discrimination text
     * for the annual IRPF Bens e Direitos declaration (as of Dec 31 or current date).
     */
    fun calculateAssetPositions(
        transactions: List<TransactionEntity>,
        targetYear: Int? = null
    ): List<IrpfAssetDeclaration> {
        val sorted = if (targetYear != null) {
            val cal = Calendar.getInstance()
            transactions.filter {
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.YEAR) <= targetYear
            }.sortedBy { it.timestamp }
        } else {
            transactions.sortedBy { it.timestamp }
        }

        val inventoryMap = mutableMapOf<String, AssetInventory>()
        val coinNames = mutableMapOf<String, String>()

        for (tx in sorted) {
            val symbol = tx.coinSymbol.uppercase()
            coinNames[symbol] = tx.coinName
            val inv = inventoryMap.getOrPut(symbol) { AssetInventory(exchange = tx.exchange) }
            inv.exchange = tx.exchange

            when (TransactionType.fromString(tx.type)) {
                TransactionType.COMPRA, TransactionType.RENDIMENTO -> {
                    inv.quantity += tx.amount
                    inv.totalCostBrl += (tx.totalBrl + tx.feeBrl)
                }
                TransactionType.VENDA -> {
                    val currentAvg = inv.averagePriceBrl
                    val toSell = tx.amount.coerceAtMost(inv.quantity)
                    inv.quantity = (inv.quantity - toSell).coerceAtLeast(0.0)
                    inv.totalCostBrl = (inv.totalCostBrl - (toSell * currentAvg)).coerceAtLeast(0.0)
                }
                TransactionType.PERMUTA -> {
                    val currentAvg = inv.averagePriceBrl
                    val toSell = tx.amount.coerceAtMost(inv.quantity)
                    inv.quantity = (inv.quantity - toSell).coerceAtLeast(0.0)
                    inv.totalCostBrl = (inv.totalCostBrl - (toSell * currentAvg)).coerceAtLeast(0.0)

                    if (!tx.targetCoinSymbol.isNullOrBlank() && (tx.targetAmount ?: 0.0) > 0.0) {
                        val targetSymbol = tx.targetCoinSymbol.uppercase()
                        coinNames[targetSymbol] = targetSymbol
                        val targetInv = inventoryMap.getOrPut(targetSymbol) { AssetInventory(exchange = tx.exchange) }
                        targetInv.quantity += (tx.targetAmount ?: 0.0)
                        targetInv.totalCostBrl += (tx.totalBrl - tx.feeBrl).coerceAtLeast(0.0)
                    }
                }
                TransactionType.TRANSFERENCIA -> {
                    if (tx.feeBrl > 0.0) inv.totalCostBrl += tx.feeBrl
                }
            }
        }

        val result = mutableListOf<IrpfAssetDeclaration>()
        for ((symbol, inv) in inventoryMap) {
            if (inv.quantity > 0.00000001 && inv.totalCostBrl > 0.01) {
                val name = coinNames[symbol] ?: symbol
                val category = IrpfCategory.detectCategory(symbol)

                val discrimination = String.format(
                    Locale("pt", "BR"),
                    "%.8f unidades do criptoativo %s (%s), adquiridas pelo custo total de R$ %,.2f (custo médio ponderado de R$ %,.2f por unidade), custodiadas na corretora %s. Apurado via CriptoFull.",
                    inv.quantity, name, symbol, inv.totalCostBrl, inv.averagePriceBrl, inv.exchange
                )

                result.add(
                    IrpfAssetDeclaration(
                        coinSymbol = symbol,
                        coinName = name,
                        irpfCategory = category,
                        quantity = inv.quantity,
                        averagePriceBrl = inv.averagePriceBrl,
                        totalAcquisitionCostBrl = inv.totalCostBrl,
                        exchangeName = inv.exchange,
                        formattedDiscrimination = discrimination
                    )
                )
            }
        }

        return result.sortedByDescending { it.totalAcquisitionCostBrl }
    }

    /**
     * Generates a complete Annual IRPF report with monthly summaries and Dec 31 asset declarations.
     */
    fun generateAnnualIrpfReport(transactions: List<TransactionEntity>, year: Int): AnnualIrpfReport {
        val allMonthly = processTransactions(transactions)
        val yearMonthly = allMonthly.filter { it.year == year }
        val assetsDec31 = calculateAssetPositions(transactions, year)

        val totalExempt = yearMonthly.sumOf { it.exemptProfitBrl }
        val totalTaxable = yearMonthly.sumOf { it.taxableProfitBrl }
        val totalTaxes = yearMonthly.sumOf { it.taxDueBrl }

        return AnnualIrpfReport(
            year = year,
            totalExemptProfitsYearBrl = totalExempt,
            totalTaxableProfitsYearBrl = totalTaxable,
            totalTaxesDueYearBrl = totalTaxes,
            assetsPositionDecember31 = assetsDec31,
            monthlyReports = yearMonthly
        )
    }

    private fun calculateDarfDueDate(year: Int, month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month) // Next month (0-indexed Calendar.MONTH: month is already next month!)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))

        // Check if weekend, adjust to previous Friday
        while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return sdf.format(cal.time)
    }
}
