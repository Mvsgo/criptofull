package com.example

import com.example.data.backup.PortfolioBackupManager
import com.example.data.local.FavoriteEntity
import com.example.data.local.PortfolioEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PortfolioBackupTest {

    @Test
    fun testExportAndImportJsonCycle() {
        val originalHoldings = listOf(
            PortfolioEntity(
                id = 1,
                coinId = "bitcoin",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                coinImage = "https://example.com/btc.png",
                amount = 1.5,
                buyPriceUsd = 62000.0,
                timestamp = 1729000000000
            ),
            PortfolioEntity(
                id = 2,
                coinId = "ethereum",
                coinSymbol = "ETH",
                coinName = "Ethereum",
                coinImage = "https://example.com/eth.png",
                amount = 10.0,
                buyPriceUsd = 3400.0,
                timestamp = 1729000100000
            )
        )
        val originalFavorites = listOf(
            FavoriteEntity("bitcoin"),
            FavoriteEntity("solana")
        )

        // Generate CriptoFull JSON
        val exportedJson = PortfolioBackupManager.createBackupJson(originalHoldings, originalFavorites)
        assertTrue(exportedJson.contains("CriptoFull"))
        assertTrue(exportedJson.contains("bitcoin"))
        assertTrue(exportedJson.contains("ethereum"))

        // Parse back the exported JSON
        val (importedHoldings, importedFavorites) = PortfolioBackupManager.parseBackupData(exportedJson)

        assertEquals(2, importedHoldings.size)
        assertEquals("bitcoin", importedHoldings[0].coinId)
        assertEquals("BTC", importedHoldings[0].coinSymbol)
        assertEquals(1.5, importedHoldings[0].amount, 0.0001)
        assertEquals(62000.0, importedHoldings[0].buyPriceUsd, 0.0001)

        assertEquals(2, importedFavorites.size)
        assertEquals("bitcoin", importedFavorites[0].coinId)
        assertEquals("solana", importedFavorites[1].coinId)
    }

    @Test
    fun testImportDirectArrayFormat() {
        val rawArrayJson = """
            [
              {"coinId": "solana", "coinSymbol": "SOL", "coinName": "Solana", "amount": 25.0, "buyPriceUsd": 140.0},
              {"coinId": "cardano", "coinSymbol": "ADA", "coinName": "Cardano", "amount": 1000.0, "buyPriceUsd": 0.40}
            ]
        """.trimIndent()

        val (holdings, favorites) = PortfolioBackupManager.parseBackupData(rawArrayJson)
        assertEquals(2, holdings.size)
        assertEquals("solana", holdings[0].coinId)
        assertEquals(25.0, holdings[0].amount, 0.0001)
        assertEquals("cardano", holdings[1].coinId)
        assertEquals(1000.0, holdings[1].amount, 0.0001)
    }

    @Test
    fun testImportCsvFormat() {
        val csv = """
            coinId,symbol,name,amount,buyPriceUsd
            bitcoin,BTC,Bitcoin,0.5,65000.0
            ethereum,ETH,Ethereum,4.0,3300.0
        """.trimIndent()

        val (holdings, _) = PortfolioBackupManager.parseBackupData(csv)
        assertEquals(2, holdings.size)
        assertEquals("bitcoin", holdings[0].coinId)
        assertEquals(0.5, holdings[0].amount, 0.0001)
        assertEquals("ethereum", holdings[1].coinId)
        assertEquals(4.0, holdings[1].amount, 0.0001)
    }
}
