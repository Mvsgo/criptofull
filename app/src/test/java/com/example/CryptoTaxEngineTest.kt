package com.example

import com.example.data.local.TransactionEntity
import com.example.domain.CryptoTaxEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CryptoTaxEngineTest {

    private val engine = CryptoTaxEngine()

    private fun createTimestamp(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 12, 0, 0)
        return cal.timeInMillis
    }

    @Test
    fun testExemptionUnder35k() {
        val txs = listOf(
            // Buy 1 BTC for R$ 300.000 in January
            TransactionEntity(
                id = 1,
                timestamp = createTimestamp(2026, 1, 10),
                dateFormatted = "10/01/2026 12:00",
                exchange = "Binance",
                type = "COMPRA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 1.0,
                unitPriceBrl = 300000.0,
                feeBrl = 0.0,
                totalBrl = 300000.0
            ),
            // Sell 0.05 BTC for R$ 18.000 in February (Total sales = 18k <= 35k -> EXEMPT)
            TransactionEntity(
                id = 2,
                timestamp = createTimestamp(2026, 2, 15),
                dateFormatted = "15/02/2026 12:00",
                exchange = "Binance",
                type = "VENDA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 0.05,
                unitPriceBrl = 360000.0,
                feeBrl = 0.0,
                totalBrl = 18000.0
            )
        )

        val reports = engine.processTransactions(txs)
        val febReport = reports.find { it.year == 2026 && it.month == 2 }

        assertTrue(febReport != null)
        assertTrue(febReport!!.isExempt)
        assertEquals(18000.0, febReport.totalSalesBrl, 0.01)
        // Profit = 18000 - (0.05 * 300000) = 18000 - 15000 = 3000
        assertEquals(3000.0, febReport.exemptProfitBrl, 0.01)
        assertEquals(0.0, febReport.taxDueBrl, 0.01)
    }

    @Test
    fun testTaxableOver35kWith15PercentDarf() {
        val txs = listOf(
            // Buy 1 BTC for R$ 300.000
            TransactionEntity(
                id = 1,
                timestamp = createTimestamp(2026, 1, 10),
                dateFormatted = "10/01/2026 12:00",
                exchange = "Binance",
                type = "COMPRA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 1.0,
                unitPriceBrl = 300000.0,
                feeBrl = 0.0,
                totalBrl = 300000.0
            ),
            // Sell 0.15 BTC for R$ 60.000 in March (Total sales = 60k > 35k -> TRIBUTÁVEL)
            TransactionEntity(
                id = 2,
                timestamp = createTimestamp(2026, 3, 20),
                dateFormatted = "20/03/2026 12:00",
                exchange = "Binance",
                type = "VENDA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 0.15,
                unitPriceBrl = 400000.0,
                feeBrl = 0.0,
                totalBrl = 60000.0
            )
        )

        val reports = engine.processTransactions(txs)
        val marchReport = reports.find { it.year == 2026 && it.month == 3 }

        assertTrue(marchReport != null)
        assertFalse(marchReport!!.isExempt)
        assertEquals(60000.0, marchReport.totalSalesBrl, 0.01)

        // Profit = 60000 - (0.15 * 300000) = 60000 - 45000 = 15000
        assertEquals(15000.0, marchReport.taxableProfitBrl, 0.01)
        // 15% DARF on 15000 = 2250.00
        assertEquals(2250.0, marchReport.taxDueBrl, 0.01)
    }

    @Test
    fun testIrpfBensEDireitosPositions() {
        val txs = listOf(
            TransactionEntity(
                id = 1,
                timestamp = createTimestamp(2026, 1, 10),
                dateFormatted = "10/01/2026 12:00",
                exchange = "Binance",
                type = "COMPRA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 0.5,
                unitPriceBrl = 300000.0,
                feeBrl = 100.0,
                totalBrl = 150000.0
            )
        )

        val positions = engine.calculateAssetPositions(txs)
        assertEquals(1, positions.size)
        val btc = positions[0]
        assertEquals("BTC", btc.coinSymbol)
        assertEquals(0.5, btc.quantity, 0.0001)
        assertEquals(150100.0, btc.totalAcquisitionCostBrl, 0.01)
        assertTrue(btc.formattedDiscrimination.contains("Bitcoin"))
        assertTrue(btc.formattedDiscrimination.contains("Binance"))
    }
}
