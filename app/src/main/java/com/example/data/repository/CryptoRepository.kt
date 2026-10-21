package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.CarteiraEntity
import com.example.data.local.CryptoDatabase
import com.example.data.local.FavoriteEntity
import com.example.data.local.LancamentoEntity
import com.example.data.local.PortfolioEntity
import com.example.data.local.TransactionEntity
import com.example.data.remote.RetrofitClient
import com.example.domain.CryptoTaxEngine
import com.example.domain.ExchangeStatementImporter
import com.example.domain.ImportStatementResult
import com.example.model.AnnualIrpfReport
import com.example.model.Coin
import com.example.model.CryptoNews
import com.example.model.IrpfAssetDeclaration
import com.example.model.MarketStats
import com.example.model.MonthlyTaxReport
import com.example.model.SparklineData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class CryptoRepository(context: Context) {
    private val database = CryptoDatabase.getDatabase(context)
    private val dao = database.cryptoDao()
    private val api = RetrofitClient.api
    private val taxEngine = CryptoTaxEngine()
    private val statementImporter = ExchangeStatementImporter()

    // Room Flows (CriptoFull SQLite)
    val carteiras: Flow<List<CarteiraEntity>> = dao.getAllCarteiras()
    val lancamentos: Flow<List<LancamentoEntity>> = dao.getAllLancamentos()
    val portfolioItems: Flow<List<PortfolioEntity>> = dao.getAllPortfolioItems()
    val favorites: Flow<List<FavoriteEntity>> = dao.getAllFavorites()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    // Carteiras CRUD
    suspend fun addCarteira(carteira: CarteiraEntity): Long {
        return withContext(Dispatchers.IO) {
            dao.insertCarteira(carteira)
        }
    }

    suspend fun updateCarteira(carteira: CarteiraEntity) {
        withContext(Dispatchers.IO) {
            dao.updateCarteira(carteira)
        }
    }

    suspend fun deleteCarteira(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteCarteiraById(id)
        }
    }

    suspend fun getCarteirasList(): List<CarteiraEntity> {
        return withContext(Dispatchers.IO) {
            dao.getAllCarteirasList()
        }
    }

    suspend fun seedDefaultCarteirasIfEmpty() {
        withContext(Dispatchers.IO) {
            val existing = dao.getAllCarteirasList()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    CarteiraEntity(nome = "Binance Principal", tipo = "EXCHANGE", corretora = "Binance", cor = "#F3BA2F", observacoes = "Conta Spot Principal"),
                    CarteiraEntity(nome = "Mercado Bitcoin", tipo = "EXCHANGE", corretora = "Mercado Bitcoin", cor = "#FF5A00", observacoes = "Conta Nacional BRL"),
                    CarteiraEntity(nome = "Metamask DeFi", tipo = "HOT_WALLET", corretora = "Metamask", cor = "#F6851B", observacoes = "Carteira Web3 EVM"),
                    CarteiraEntity(nome = "Carteira Fria (Ledger)", tipo = "COLD_WALLET", corretora = "Ledger", cor = "#00E5FF", observacoes = "Hardware Wallet Custódia Segura")
                )
                dao.insertCarteiras(defaults)
            }
        }
    }

    // Lançamentos CRUD (com sincronização com motor fiscal)
    suspend fun addLancamento(lancamento: LancamentoEntity): Long {
        return withContext(Dispatchers.IO) {
            val id = dao.insertLancamento(lancamento)
            dao.insertTransaction(lancamento.copy(id = id).toTransactionEntity())
            id
        }
    }

    suspend fun updateLancamento(lancamento: LancamentoEntity) {
        withContext(Dispatchers.IO) {
            dao.updateLancamento(lancamento)
            dao.insertTransaction(lancamento.toTransactionEntity())
        }
    }

    suspend fun deleteLancamento(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteLancamentoById(id)
            dao.deleteTransactionById(id)
        }
    }

    suspend fun clearLancamentos() {
        withContext(Dispatchers.IO) {
            dao.clearLancamentos()
            dao.clearTransactions()
        }
    }

    suspend fun addTransaction(tx: TransactionEntity): Long {
        return withContext(Dispatchers.IO) {
            val lancamento = LancamentoEntity.fromTransactionEntity(tx)
            val id = dao.insertLancamento(lancamento)
            dao.insertTransaction(tx.copy(id = id))
            id
        }
    }

    suspend fun addTransactions(list: List<TransactionEntity>) {
        withContext(Dispatchers.IO) {
            dao.insertTransactions(list)
            dao.insertLancamentos(list.map { LancamentoEntity.fromTransactionEntity(it) })
        }
    }

    suspend fun deleteTransaction(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteTransactionById(id)
            dao.deleteLancamentoById(id)
        }
    }

    suspend fun clearTransactions() {
        withContext(Dispatchers.IO) {
            dao.clearTransactions()
            dao.clearLancamentos()
        }
    }

    suspend fun importStatement(rawContent: String, defaultExchange: String): ImportStatementResult {
        return withContext(Dispatchers.IO) {
            val res = statementImporter.parseStatement(rawContent, defaultExchange)
            if (res.success && res.transactions.isNotEmpty()) {
                dao.insertTransactions(res.transactions)
                dao.insertLancamentos(res.transactions.map { LancamentoEntity.fromTransactionEntity(it) })
            }
            res
        }
    }

    fun calculateMonthlyReports(txList: List<TransactionEntity>): List<MonthlyTaxReport> {
        return taxEngine.processTransactions(txList)
    }

    fun calculateAssetPositions(txList: List<TransactionEntity>, year: Int? = null): List<IrpfAssetDeclaration> {
        return taxEngine.calculateAssetPositions(txList, year)
    }

    fun generateAnnualReport(txList: List<TransactionEntity>, year: Int): AnnualIrpfReport {
        return taxEngine.generateAnnualIrpfReport(txList, year)
    }

    suspend fun seedSampleTaxDataIfEmpty() {
        withContext(Dispatchers.IO) {
            val existing = dao.getAllTransactionsList()
            if (existing.isEmpty()) {
                dao.insertTransactions(getSampleTaxTransactions())
            }
        }
    }

    suspend fun addPortfolioItem(
        coin: Coin,
        amount: Double,
        buyPriceUsd: Double
    ) {
        withContext(Dispatchers.IO) {
            dao.insertPortfolioItem(
                PortfolioEntity(
                    coinId = coin.id,
                    coinSymbol = coin.symbol.uppercase(),
                    coinName = coin.name,
                    coinImage = coin.image,
                    amount = amount,
                    buyPriceUsd = buyPriceUsd
                )
            )
        }
    }

    suspend fun removePortfolioItem(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deletePortfolioItemById(id)
        }
    }

    suspend fun toggleFavorite(coinId: String, isFav: Boolean) {
        withContext(Dispatchers.IO) {
            if (isFav) {
                dao.removeFavorite(coinId)
            } else {
                dao.addFavorite(FavoriteEntity(coinId = coinId))
            }
        }
    }

    suspend fun exportPortfolioJson(): String {
        return withContext(Dispatchers.IO) {
            val carteirasList = dao.getAllCarteirasList()
            val lancamentosList = dao.getAllLancamentosList()
            val portfolio = dao.getAllPortfolioItemsList()
            val favorites = dao.getAllFavoritesList()
            com.example.data.backup.PortfolioBackupManager.createFullBackupJson(carteirasList, lancamentosList, portfolio, favorites)
        }
    }

    suspend fun importPortfolioData(rawContent: String, merge: Boolean): com.example.data.backup.BackupImportResult {
        return withContext(Dispatchers.IO) {
            try {
                val fullBackup = com.example.data.backup.PortfolioBackupManager.parseFullBackupData(rawContent)
                val hasAnyData = fullBackup.carteiras.isNotEmpty() || fullBackup.lancamentos.isNotEmpty() ||
                        fullBackup.portfolio.isNotEmpty() || fullBackup.favorites.isNotEmpty()

                if (!hasAnyData) {
                    return@withContext com.example.data.backup.BackupImportResult(
                        success = false,
                        importedHoldingsCount = 0,
                        importedFavoritesCount = 0,
                        message = "Nenhum dado válido de carteiras, lançamentos ou ativos encontrado no arquivo."
                    )
                }

                if (!merge) {
                    if (fullBackup.carteiras.isNotEmpty()) dao.clearCarteiras()
                    if (fullBackup.lancamentos.isNotEmpty()) {
                        dao.clearLancamentos()
                        dao.clearTransactions()
                    }
                    if (fullBackup.portfolio.isNotEmpty()) dao.clearPortfolio()
                    if (fullBackup.favorites.isNotEmpty()) dao.clearFavorites()
                }

                if (fullBackup.carteiras.isNotEmpty()) {
                    dao.insertCarteiras(fullBackup.carteiras)
                }
                if (fullBackup.lancamentos.isNotEmpty()) {
                    dao.insertLancamentos(fullBackup.lancamentos)
                    dao.insertTransactions(fullBackup.lancamentos.map { it.toTransactionEntity() })
                }
                if (fullBackup.portfolio.isNotEmpty()) {
                    dao.insertPortfolioItems(fullBackup.portfolio)
                }
                if (fullBackup.favorites.isNotEmpty()) {
                    dao.insertFavorites(fullBackup.favorites)
                }

                com.example.data.backup.BackupImportResult(
                    success = true,
                    importedHoldingsCount = fullBackup.portfolio.size,
                    importedFavoritesCount = fullBackup.favorites.size,
                    importedCarteirasCount = fullBackup.carteiras.size,
                    importedLancamentosCount = fullBackup.lancamentos.size,
                    message = "Dados importados com sucesso: ${fullBackup.carteiras.size} carteiras, ${fullBackup.lancamentos.size} lançamentos, ${fullBackup.portfolio.size} ativos."
                )
            } catch (e: Exception) {
                com.example.data.backup.BackupImportResult(
                    success = false,
                    importedHoldingsCount = 0,
                    importedFavoritesCount = 0,
                    message = "Falha ao processar arquivo: ${e.localizedMessage ?: "Formato inválido"}"
                )
            }
        }
    }

    suspend fun fetchMarketCoins(): List<Coin> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getCoinsMarkets(
                    vsCurrency = "usd",
                    order = "market_cap_desc",
                    perPage = 50,
                    page = 1,
                    sparkline = true
                )
                if (response.isNotEmpty()) {
                    // Enrich response with local descriptions and categories if missing
                    response.map { enrichCoin(it) }
                } else {
                    getFallbackCoins()
                }
            } catch (e: Exception) {
                Log.w("CryptoRepository", "CoinGecko API failed or rate limited, using cached dataset", e)
                getFallbackCoins()
            }
        }
    }

    private fun enrichCoin(coin: Coin): Coin {
        val category = when (coin.symbol.lowercase()) {
            "btc", "eth", "sol", "ada", "avax", "dot", "near", "sui", "kas", "atom" -> "Layer 1"
            "uni", "link", "aave", "mkr", "crv" -> "DeFi"
            "doge", "shib", "pepe", "wif", "bonk" -> "Memes"
            "rndr", "near", "fet", "tao" -> "IA & Big Data"
            "arb", "op", "matic" -> "Layer 2"
            else -> "Criptoativo"
        }
        val description = getCoinDescription(coin.symbol.lowercase(), coin.name)
        return coin.copy(category = category, description = description)
    }

    fun getMarketStats(): MarketStats {
        return MarketStats(
            totalMarketCapUsd = 2_720_000_000_000.0,
            total24hVolumeUsd = 88_400_000_000.0,
            btcDominancePercentage = 57.2,
            ethDominancePercentage = 14.1,
            marketCapChangePercentage24h = 3.12,
            fearAndGreedIndex = 72,
            fearAndGreedLabel = "Ganância"
        )
    }

    fun getNews(): List<CryptoNews> {
        return listOf(
            CryptoNews(
                id = "1",
                title = "Bitcoin atinge nova máxima histórica com entrada recorde de ETFs institucionais",
                summary = "Fluxos líquidos em fundos de índice de Bitcoin à vista ultrapassam marcas históricas em Wall Street.",
                source = "CriptoFull News",
                timeAgo = "15m atrás",
                category = "Bitcoin",
                sentiment = "Positivo",
                readMinutes = 2
            ),
            CryptoNews(
                id = "2",
                title = "Ethereum registra aumento de 40% no volume de staking após nova atualização",
                summary = "A rede consolidou mais de 33 milhões de ETH em staking, elevando a segurança e taxas de rendimento.",
                source = "DeFi Daily",
                timeAgo = "1h atrás",
                category = "DeFi",
                sentiment = "Positivo",
                readMinutes = 3
            ),
            CryptoNews(
                id = "3",
                title = "Solana supera marcas de transações por segundo e atrai novos protocolos DeFi",
                summary = "O ecossistema Solana experimenta expansão veloz com taxas insignificantes e alta liquidez de stablecoins.",
                source = "Blockchain Insights",
                timeAgo = "3h atrás",
                category = "Layer 1",
                sentiment = "Positivo",
                readMinutes = 4
            ),
            CryptoNews(
                id = "4",
                title = "Banco Central do Brasil avança testes da 2ª fase do Drex com contratos inteligentes",
                summary = "Nova etapa avalia privacidade de dados e liquidação de títulos públicos tokenizados.",
                source = "Fintech Brasil",
                timeAgo = "5h atrás",
                category = "Regulação",
                sentiment = "Neutro",
                readMinutes = 3
            ),
            CryptoNews(
                id = "5",
                title = "Tokens de Inteligência Artificial lideram valorizações da semana no mercado cripto",
                summary = "Projetos descentralizados de GPU e computação como Render e Near superam o desempenho geral.",
                source = "AI Web3 Report",
                timeAgo = "7h atrás",
                category = "Airdrop",
                sentiment = "Positivo",
                readMinutes = 2
            )
        )
    }

    private fun getCoinDescription(symbol: String, name: String): String {
        return when (symbol) {
            "btc" -> "O Bitcoin é a primeira e mais conhecida criptomoeda descentralizada do mundo, criada em 2008 por Satoshi Nakamoto para permitir pagamentos ponto a ponto sem intermediários."
            "eth" -> "Ethereum é uma plataforma blockchain global descentralizada que executa contratos inteligentes (smart contracts) e suporta aplicações descentralizadas (DApps) e finanças abertas."
            "sol" -> "Solana é uma blockchain de altíssimo desempenho projetada para adoção em massa, conhecida por sua alta velocidade de transações e taxas extremamente baixas."
            "bnb" -> "BNB é o token nativo do ecossistema BNB Chain e da maior corretora de ativos digitais global, usado para pagar taxas com desconto e em finanças descentralizadas."
            "xrp" -> "XRP é a moeda digital desenvolvida pela Ripple Labs para permitir liquidação rápida e de custo quase nulo em pagamentos transfronteiriços entre instituições financeiras."
            "ada" -> "Cardano é uma plataforma blockchain de terceira geração baseada em pesquisa acadêmica rigorosa revisada por pares, focada em segurança, escalabilidade e sustentabilidade."
            "doge" -> "Dogecoin é uma criptomoeda baseada em meme da internet com uma vibrante comunidade global, amplamente utilizada para gorjetas e microtransações rápidas."
            "avax" -> "Avalanche é uma plataforma de contratos inteligentes ultrarrápida com sub-redes customizáveis e finalização de transações em menos de um segundo."
            "dot" -> "Polkadot conecta e protege blockchains especializadas (parachains), permitindo a transferência transparente de dados e ativos entre diferentes redes."
            "link" -> "Chainlink é a rede de oráculos descentralizada padrão da indústria, conectando contratos inteligentes do mundo cripto com dados reais e APIs externas."
            else -> "$name ($symbol) é um ativo digital criptográfico transacionado em blockchains globais descentralizadas com suporte a liquidez contínua e auto-custódia."
        }
    }

    fun getFallbackCoins(): List<Coin> {
        return listOf(
            Coin(
                id = "bitcoin",
                symbol = "btc",
                name = "Bitcoin",
                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
                currentPrice = 67850.0,
                marketCap = 1_340_000_000_000.0,
                marketCapRank = 1,
                totalVolume = 32_500_000_000.0,
                high24h = 68400.0,
                low24h = 66100.0,
                priceChange24h = 1750.0,
                priceChangePercentage24h = 2.65,
                circulatingSupply = 19_770_000.0,
                allTimeHigh = 73750.0,
                sparklineIn7d = SparklineData(listOf(62500.0, 63100.0, 64200.0, 63800.0, 65500.0, 66800.0, 67850.0)),
                category = "Layer 1",
                description = getCoinDescription("btc", "Bitcoin")
            ),
            Coin(
                id = "ethereum",
                symbol = "eth",
                name = "Ethereum",
                image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png",
                currentPrice = 3540.0,
                marketCap = 425_000_000_000.0,
                marketCapRank = 2,
                totalVolume = 18_200_000_000.0,
                high24h = 3590.0,
                low24h = 3420.0,
                priceChange24h = 120.0,
                priceChangePercentage24h = 3.51,
                circulatingSupply = 120_200_000.0,
                allTimeHigh = 4891.0,
                sparklineIn7d = SparklineData(listOf(3280.0, 3320.0, 3410.0, 3390.0, 3480.0, 3510.0, 3540.0)),
                category = "Layer 1",
                description = getCoinDescription("eth", "Ethereum")
            ),
            Coin(
                id = "solana",
                symbol = "sol",
                name = "Solana",
                image = "https://assets.coingecko.com/coins/images/4128/large/solana.png",
                currentPrice = 178.50,
                marketCap = 84_000_000_000.0,
                marketCapRank = 3,
                totalVolume = 6_400_000_000.0,
                high24h = 182.0,
                low24h = 169.5,
                priceChange24h = 8.5,
                priceChangePercentage24h = 5.0,
                circulatingSupply = 470_000_000.0,
                allTimeHigh = 260.0,
                sparklineIn7d = SparklineData(listOf(152.0, 158.0, 164.0, 162.0, 171.0, 175.0, 178.5)),
                category = "Layer 1",
                description = getCoinDescription("sol", "Solana")
            ),
            Coin(
                id = "binancecoin",
                symbol = "bnb",
                name = "BNB",
                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png",
                currentPrice = 595.20,
                marketCap = 87_000_000_000.0,
                marketCapRank = 4,
                totalVolume = 1_250_000_000.0,
                high24h = 605.0,
                low24h = 588.0,
                priceChange24h = 7.20,
                priceChangePercentage24h = 1.22,
                circulatingSupply = 146_000_000.0,
                allTimeHigh = 720.0,
                sparklineIn7d = SparklineData(listOf(575.0, 580.0, 585.0, 582.0, 590.0, 592.0, 595.2)),
                category = "Layer 1",
                description = getCoinDescription("bnb", "BNB")
            ),
            Coin(
                id = "ripple",
                symbol = "xrp",
                name = "XRP",
                image = "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png",
                currentPrice = 0.584,
                marketCap = 33_000_000_000.0,
                marketCapRank = 5,
                totalVolume = 1_850_000_000.0,
                high24h = 0.602,
                low24h = 0.571,
                priceChange24h = -0.012,
                priceChangePercentage24h = -2.01,
                circulatingSupply = 56_000_000_000.0,
                allTimeHigh = 3.84,
                sparklineIn7d = SparklineData(listOf(0.55, 0.57, 0.59, 0.61, 0.60, 0.59, 0.584)),
                category = "Layer 1",
                description = getCoinDescription("xrp", "XRP")
            ),
            Coin(
                id = "cardano",
                symbol = "ada",
                name = "Cardano",
                image = "https://assets.coingecko.com/coins/images/975/large/cardano.png",
                currentPrice = 0.425,
                marketCap = 15_200_000_000.0,
                marketCapRank = 6,
                totalVolume = 480_000_000.0,
                high24h = 0.442,
                low24h = 0.418,
                priceChange24h = 0.007,
                priceChangePercentage24h = 1.67,
                circulatingSupply = 35_700_000_000.0,
                allTimeHigh = 3.10,
                sparklineIn7d = SparklineData(listOf(0.39, 0.40, 0.41, 0.415, 0.42, 0.422, 0.425)),
                category = "Layer 1",
                description = getCoinDescription("ada", "Cardano")
            ),
            Coin(
                id = "dogecoin",
                symbol = "doge",
                name = "Dogecoin",
                image = "https://assets.coingecko.com/coins/images/5/large/dogecoin.png",
                currentPrice = 0.142,
                marketCap = 20_800_000_000.0,
                marketCapRank = 7,
                totalVolume = 1_620_000_000.0,
                high24h = 0.149,
                low24h = 0.134,
                priceChange24h = 0.008,
                priceChangePercentage24h = 5.97,
                circulatingSupply = 146_000_000_000.0,
                allTimeHigh = 0.737,
                sparklineIn7d = SparklineData(listOf(0.12, 0.125, 0.13, 0.128, 0.135, 0.139, 0.142)),
                category = "Memes",
                description = getCoinDescription("doge", "Dogecoin")
            ),
            Coin(
                id = "avalanche-2",
                symbol = "avax",
                name = "Avalanche",
                image = "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png",
                currentPrice = 28.90,
                marketCap = 11_800_000_000.0,
                marketCapRank = 8,
                totalVolume = 420_000_000.0,
                high24h = 29.80,
                low24h = 27.50,
                priceChange24h = 1.40,
                priceChangePercentage24h = 5.09,
                circulatingSupply = 407_000_000.0,
                allTimeHigh = 146.22,
                sparklineIn7d = SparklineData(listOf(24.5, 25.2, 26.0, 26.8, 27.5, 28.1, 28.9)),
                category = "Layer 1",
                description = getCoinDescription("avax", "Avalanche")
            ),
            Coin(
                id = "chainlink",
                symbol = "link",
                name = "Chainlink",
                image = "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png",
                currentPrice = 14.85,
                marketCap = 9_200_000_000.0,
                marketCapRank = 9,
                totalVolume = 390_000_000.0,
                high24h = 15.20,
                low24h = 14.10,
                priceChange24h = 0.75,
                priceChangePercentage24h = 5.32,
                circulatingSupply = 608_000_000.0,
                allTimeHigh = 52.88,
                sparklineIn7d = SparklineData(listOf(12.8, 13.2, 13.8, 14.1, 14.4, 14.6, 14.85)),
                category = "DeFi",
                description = getCoinDescription("link", "Chainlink")
            ),
            Coin(
                id = "polkadot",
                symbol = "dot",
                name = "Polkadot",
                image = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png",
                currentPrice = 4.85,
                marketCap = 7_100_000_000.0,
                marketCapRank = 10,
                totalVolume = 210_000_000.0,
                high24h = 4.98,
                low24h = 4.70,
                priceChange24h = -0.05,
                priceChangePercentage24h = -1.02,
                circulatingSupply = 1_460_000_000.0,
                allTimeHigh = 55.0,
                sparklineIn7d = SparklineData(listOf(4.6, 4.7, 4.8, 4.9, 4.95, 4.9, 4.85)),
                category = "Layer 1",
                description = getCoinDescription("dot", "Polkadot")
            ),
            Coin(
                id = "near",
                symbol = "near",
                name = "NEAR Protocol",
                image = "https://assets.coingecko.com/coins/images/10365/large/near.png",
                currentPrice = 5.40,
                marketCap = 6_500_000_000.0,
                marketCapRank = 11,
                totalVolume = 510_000_000.0,
                high24h = 5.65,
                low24h = 5.12,
                priceChange24h = 0.28,
                priceChangePercentage24h = 5.47,
                circulatingSupply = 1_210_000_000.0,
                allTimeHigh = 20.42,
                sparklineIn7d = SparklineData(listOf(4.5, 4.7, 4.9, 5.0, 5.2, 5.35, 5.40)),
                category = "IA & Big Data",
                description = "NEAR Protocol é uma rede em nuvem descentralizada projetada para escalabilidade ilimitada e execução de inteligência artificial Web3."
            ),
            Coin(
                id = "sui",
                symbol = "sui",
                name = "Sui",
                image = "https://assets.coingecko.com/coins/images/26375/large/sui-ocean-square.png",
                currentPrice = 2.35,
                marketCap = 6_600_000_000.0,
                marketCapRank = 12,
                totalVolume = 920_000_000.0,
                high24h = 2.48,
                low24h = 2.18,
                priceChange24h = 0.17,
                priceChangePercentage24h = 7.80,
                circulatingSupply = 2_800_000_000.0,
                allTimeHigh = 2.50,
                sparklineIn7d = SparklineData(listOf(1.85, 1.95, 2.05, 2.15, 2.22, 2.28, 2.35)),
                category = "Layer 1",
                description = "Sui é uma blockchain Layer 1 desenvolvida por ex-engenheiros da Meta com execução paralela ultrarrápida usando a linguagem Move."
            ),
            Coin(
                id = "render-token",
                symbol = "rndr",
                name = "Render",
                image = "https://assets.coingecko.com/coins/images/11636/large/render.png",
                currentPrice = 6.85,
                marketCap = 3_600_000_000.0,
                marketCapRank = 13,
                totalVolume = 320_000_000.0,
                high24h = 7.15,
                low24h = 6.45,
                priceChange24h = 0.40,
                priceChangePercentage24h = 6.20,
                circulatingSupply = 518_000_000.0,
                allTimeHigh = 13.60,
                sparklineIn7d = SparklineData(listOf(5.6, 5.8, 6.1, 6.3, 6.5, 6.7, 6.85)),
                category = "IA & Big Data",
                description = "Render Network é um provedor líder de soluções computacionais distribuídas de renderização por GPU e IA descentralizada."
            ),
            Coin(
                id = "uniswap",
                symbol = "uni",
                name = "Uniswap",
                image = "https://assets.coingecko.com/coins/images/12504/large/uniswap-uni.png",
                currentPrice = 8.10,
                marketCap = 4_850_000_000.0,
                marketCapRank = 14,
                totalVolume = 280_000_000.0,
                high24h = 8.35,
                low24h = 7.80,
                priceChange24h = 0.30,
                priceChangePercentage24h = 3.85,
                circulatingSupply = 600_000_000.0,
                allTimeHigh = 44.97,
                sparklineIn7d = SparklineData(listOf(7.2, 7.4, 7.6, 7.8, 7.9, 8.0, 8.10)),
                category = "DeFi",
                description = "Uniswap é o maior protocolo descentralizado de negociação automatizada (AMM) de criptoativos do ecossistema Ethereum."
            ),
            Coin(
                id = "shiba-inu",
                symbol = "shib",
                name = "Shiba Inu",
                image = "https://assets.coingecko.com/coins/images/11939/large/shiba.png",
                currentPrice = 0.0000185,
                marketCap = 10_900_000_000.0,
                marketCapRank = 15,
                totalVolume = 480_000_000.0,
                high24h = 0.0000192,
                low24h = 0.0000178,
                priceChange24h = 0.0000007,
                priceChangePercentage24h = 3.93,
                circulatingSupply = 589_000_000_000_000.0,
                allTimeHigh = 0.000088,
                sparklineIn7d = SparklineData(listOf(0.000016, 0.0000165, 0.000017, 0.0000175, 0.000018, 0.0000182, 0.0000185)),
                category = "Memes",
            )
        )
    }

    private fun getSampleTaxTransactions(): List<TransactionEntity> {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)

        return listOf(
            TransactionEntity(
                timestamp = getTime(year, 1, 10, 10, 30),
                dateFormatted = "10/01/$year 10:30",
                exchange = "Binance",
                type = "COMPRA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 0.20,
                unitPriceBrl = 320000.0,
                feeBrl = 32.0,
                totalBrl = 64000.0,
                notes = "Aporte inicial em Bitcoin"
            ),
            TransactionEntity(
                timestamp = getTime(year, 1, 15, 14, 20),
                dateFormatted = "15/01/$year 14:20",
                exchange = "Mercado Bitcoin",
                type = "COMPRA",
                coinSymbol = "ETH",
                coinName = "Ethereum",
                amount = 2.0,
                unitPriceBrl = 16000.0,
                feeBrl = 24.0,
                totalBrl = 32000.0,
                notes = "Aporte em Ethereum"
            ),
            TransactionEntity(
                timestamp = getTime(year, 2, 18, 16, 45),
                dateFormatted = "18/02/$year 16:45",
                exchange = "Binance",
                type = "VENDA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 0.05,
                unitPriceBrl = 380000.0,
                feeBrl = 19.0,
                totalBrl = 19000.0,
                notes = "Realização parcial (Venda <= 35k: ISENTO)"
            ),
            TransactionEntity(
                timestamp = getTime(year, 3, 5, 11, 0),
                dateFormatted = "05/03/$year 11:00",
                exchange = "Bybit",
                type = "COMPRA",
                coinSymbol = "SOL",
                coinName = "Solana",
                amount = 40.0,
                unitPriceBrl = 800.0,
                feeBrl = 16.0,
                totalBrl = 32000.0,
                notes = "Entrada em Solana"
            ),
            TransactionEntity(
                timestamp = getTime(year, 4, 12, 15, 30),
                dateFormatted = "12/04/$year 15:30",
                exchange = "Binance",
                type = "VENDA",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                amount = 0.10,
                unitPriceBrl = 420000.0,
                feeBrl = 42.0,
                totalBrl = 42000.0,
                notes = "Venda acima de R$ 35k: TRIBUTÁVEL com DARF 15%"
            )
        )
    }

    private fun getTime(year: Int, month: Int, day: Int, hour: Int, min: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, hour, min, 0)
        return cal.timeInMillis
    }
}
