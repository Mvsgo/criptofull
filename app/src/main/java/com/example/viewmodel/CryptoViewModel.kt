package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupImportResult
import com.example.data.local.CarteiraEntity
import com.example.data.local.LancamentoEntity
import com.example.data.local.PortfolioEntity
import com.example.data.local.TransactionEntity
import com.example.data.repository.CryptoRepository
import com.example.domain.ImportStatementResult
import com.example.model.AnnualIrpfReport
import com.example.model.Coin
import com.example.model.CryptoNews
import com.example.model.Currency
import com.example.model.IrpfAssetDeclaration
import com.example.model.MarketStats
import com.example.model.MonthlyTaxReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class Screen {
    object Apuracao : Screen()
    object Operacoes : Screen()
    object Carteiras : Screen()
    object Importar : Screen()
    object Relatorios : Screen()
    object Estoque : Screen()
    object Mercado : Screen()
    data class CoinDetail(val coin: Coin) : Screen()
}

data class PortfolioSummary(
    val totalValueUsd: Double = 0.0,
    val totalInvestedUsd: Double = 0.0,
    val totalProfitLossUsd: Double = 0.0,
    val totalProfitLossPercent: Double = 0.0,
    val items: List<PortfolioHoldingUi> = emptyList()
)

data class PortfolioHoldingUi(
    val entity: PortfolioEntity,
    val currentCoin: Coin?,
    val currentTotalValueUsd: Double,
    val investedTotalUsd: Double,
    val profitLossUsd: Double,
    val profitLossPercent: Double
)

class CryptoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CryptoRepository(application)

    // Current navigation screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Apuracao)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Carteiras & Lançamentos (CriptoFull SQLite)
    val carteiras: StateFlow<List<CarteiraEntity>> = repository.carteiras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lancamentos: StateFlow<List<LancamentoEntity>> = repository.lancamentos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tax Engine & Statements State
    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Tax Reports calculated by the Tax Engine
    val monthlyReports: StateFlow<List<MonthlyTaxReport>> = transactions
        .map { txList -> repository.calculateMonthlyReports(txList) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Asset Positions & Average Acquisition Cost
    val assetPositions: StateFlow<List<IrpfAssetDeclaration>> = transactions
        .map { txList -> repository.calculateAssetPositions(txList) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Year for Annual IRPF Declaration
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Annual IRPF Report
    val annualReport: StateFlow<AnnualIrpfReport?> = combine(transactions, _selectedYear) { txList, year ->
        repository.generateAnnualReport(txList, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Selected Month Report for detailed view modal
    private val _selectedMonthReport = MutableStateFlow<MonthlyTaxReport?>(null)
    val selectedMonthReport: StateFlow<MonthlyTaxReport?> = _selectedMonthReport.asStateFlow()

    // Coins & Market
    private val _coins = MutableStateFlow<List<Coin>>(emptyList())
    val coins: StateFlow<List<Coin>> = _coins.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(Currency.BRL)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()

    private val _marketStats = MutableStateFlow(repository.getMarketStats())
    val marketStats: StateFlow<MarketStats> = _marketStats.asStateFlow()

    private val _newsList = MutableStateFlow<List<CryptoNews>>(repository.getNews())
    val newsList: StateFlow<List<CryptoNews>> = _newsList.asStateFlow()

    val favoritesSet: StateFlow<Set<String>> = repository.favorites
        .combine(_coins) { favEntities, _ ->
            favEntities.map { it.coinId }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val portfolioEntities: StateFlow<List<PortfolioEntity>> = repository.portfolioItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolioSummary: StateFlow<PortfolioSummary> = combine(
        portfolioEntities,
        _coins
    ) { items, coinList ->
        val coinMap = coinList.associateBy { it.id }
        var totalVal = 0.0
        var totalInv = 0.0

        val holdingUiList = items.map { entity ->
            val coin = coinMap[entity.coinId]
            val currentPrice = coin?.currentPrice ?: entity.buyPriceUsd
            val currentVal = entity.amount * currentPrice
            val invested = entity.amount * entity.buyPriceUsd
            val pnl = currentVal - invested
            val pnlPct = if (invested > 0) (pnl / invested) * 100 else 0.0

            totalVal += currentVal
            totalInv += invested

            PortfolioHoldingUi(
                entity = entity,
                currentCoin = coin,
                currentTotalValueUsd = currentVal,
                investedTotalUsd = invested,
                profitLossUsd = pnl,
                profitLossPercent = pnlPct
            )
        }

        val totalPnl = totalVal - totalInv
        val totalPnlPct = if (totalInv > 0) (totalPnl / totalInv) * 100 else 0.0

        PortfolioSummary(
            totalValueUsd = totalVal,
            totalInvestedUsd = totalInv,
            totalProfitLossUsd = totalPnl,
            totalProfitLossPercent = totalPnlPct,
            items = holdingUiList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioSummary())

    val filteredCoins: StateFlow<List<Coin>> = combine(
        _coins,
        _searchQuery,
        _selectedCategory,
        favoritesSet
    ) { allCoins, query, category, favs ->
        var list = allCoins
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) || it.symbol.lowercase().contains(q)
            }
        }
        when (category) {
            "Top 10" -> list.take(10)
            "Altas 🚀" -> list.filter { it.priceChangePercentage24h > 0 }.sortedByDescending { it.priceChangePercentage24h }
            "Baixas 🔻" -> list.filter { it.priceChangePercentage24h < 0 }.sortedBy { it.priceChangePercentage24h }
            "Favoritos ⭐" -> list.filter { favs.contains(it.id) }
            "Layer 1" -> list.filter { it.category == "Layer 1" }
            "DeFi" -> list.filter { it.category == "DeFi" }
            "IA" -> list.filter { it.category == "IA & Big Data" }
            "Memes" -> list.filter { it.category == "Memes" }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
        seedSampleDataIfEmpty()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val loadedCoins = repository.fetchMarketCoins()
            _coins.value = loadedCoins
            _isLoading.value = false
        }
    }

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            repository.seedDefaultCarteirasIfEmpty()
            repository.seedSampleTaxDataIfEmpty()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val loadedCoins = repository.fetchMarketCoins()
            _coins.value = loadedCoins
            _isRefreshing.value = false
        }
    }

    // Carteiras Actions (CriptoFull)
    fun addCarteira(
        nome: String,
        tipo: String,
        corretora: String,
        endereco: String? = null,
        cor: String = "#00E5FF",
        observacoes: String? = null,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val carteira = CarteiraEntity(
                nome = nome.trim(),
                tipo = tipo,
                corretora = corretora,
                endereco = endereco?.trim()?.ifBlank { null },
                cor = cor,
                observacoes = observacoes?.trim()?.ifBlank { null }
            )
            repository.addCarteira(carteira)
            onComplete?.invoke()
        }
    }

    fun updateCarteira(carteira: CarteiraEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.updateCarteira(carteira)
            onComplete?.invoke()
        }
    }

    fun deleteCarteira(id: Long) {
        viewModelScope.launch {
            repository.deleteCarteira(id)
        }
    }

    // Lançamentos Actions (CriptoFull)
    fun addLancamento(lancamento: LancamentoEntity) {
        viewModelScope.launch {
            repository.addLancamento(lancamento)
        }
    }

    fun addLancamento(
        carteiraId: Long?,
        carteiraNome: String,
        tipoOperacao: String,
        dataHora: String,
        ativoPrincipal: String,
        nomeAtivo: String,
        quantidade: Double,
        precoUnitario: Double,
        valorTotal: Double,
        moedaCotacao: String = "BRL",
        taxa: Double = 0.0,
        moedaTaxa: String = "BRL",
        valorTaxaBrl: Double = 0.0,
        ativoSecundario: String? = null,
        quantidadeSecundaria: Double? = null,
        precoUnitarioSecundario: Double? = null,
        carteiraDestinoId: Long? = null,
        carteiraDestinoNome: String? = null,
        txHash: String? = null,
        observacoes: String? = null
    ) {
        viewModelScope.launch {
            val lancamento = LancamentoEntity(
                carteiraId = carteiraId,
                carteiraNome = carteiraNome,
                tipoOperacao = tipoOperacao.uppercase(),
                dataHora = dataHora,
                timestamp = System.currentTimeMillis(),
                ativoPrincipal = ativoPrincipal.uppercase().trim(),
                nomeAtivo = nomeAtivo.trim(),
                quantidade = quantidade,
                precoUnitario = precoUnitario,
                valorTotal = valorTotal,
                moedaCotacao = moedaCotacao,
                taxa = taxa,
                moedaTaxa = moedaTaxa,
                valorTaxaBrl = valorTaxaBrl,
                ativoSecundario = ativoSecundario?.uppercase()?.trim()?.ifBlank { null },
                quantidadeSecundaria = quantidadeSecundaria,
                precoUnitarioSecundario = precoUnitarioSecundario,
                carteiraDestinoId = carteiraDestinoId,
                carteiraDestinoNome = carteiraDestinoNome?.trim()?.ifBlank { null },
                txHash = txHash?.trim()?.ifBlank { null },
                observacoes = observacoes?.trim()?.ifBlank { null }
            )
            repository.addLancamento(lancamento)
        }
    }

    fun deleteLancamento(id: Long) {
        viewModelScope.launch {
            repository.deleteLancamento(id)
        }
    }

    fun clearAllLancamentos() {
        viewModelScope.launch {
            repository.clearLancamentos()
        }
    }

    // Tax Engine Actions (Legacy / Bridge)
    fun addTransaction(
        dateFormatted: String,
        exchange: String,
        type: String,
        symbol: String,
        coinName: String,
        amount: Double,
        unitPriceBrl: Double,
        feeBrl: Double,
        totalBrl: Double,
        targetSymbol: String? = null,
        targetAmount: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val entity = TransactionEntity(
                timestamp = timestamp,
                dateFormatted = dateFormatted,
                exchange = exchange,
                type = type,
                coinSymbol = symbol.uppercase(),
                coinName = coinName,
                amount = amount,
                unitPriceBrl = unitPriceBrl,
                feeBrl = feeBrl,
                totalBrl = totalBrl,
                targetCoinSymbol = targetSymbol,
                targetAmount = targetAmount,
                notes = notes
            )
            repository.addTransaction(entity)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearTransactions()
        }
    }

    fun importExchangeStatement(
        rawContent: String,
        defaultExchange: String,
        onResult: (ImportStatementResult) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.importStatement(rawContent, defaultExchange)
            onResult(res)
        }
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    fun setSelectedMonthReport(report: MonthlyTaxReport?) {
        _selectedMonthReport.value = report
    }

    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(coinId: String) {
        viewModelScope.launch {
            val isFav = favoritesSet.value.contains(coinId)
            repository.toggleFavorite(coinId, isFav)
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (_currentScreen.value is Screen.CoinDetail) {
            _currentScreen.value = Screen.Mercado
        } else {
            _currentScreen.value = Screen.Apuracao
        }
    }

    fun exportPortfolio(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportPortfolioJson()
            onResult(json)
        }
    }

    fun importPortfolio(rawContent: String, merge: Boolean, onResult: (BackupImportResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.importPortfolioData(rawContent, merge)
            onResult(result)
        }
    }

    fun addPortfolioTransaction(coin: Coin, amount: Double, buyPriceUsd: Double) {
        viewModelScope.launch {
            repository.addPortfolioItem(coin, amount, buyPriceUsd)
        }
    }

    fun removePortfolioItem(id: Long) {
        viewModelScope.launch {
            repository.removePortfolioItem(id)
        }
    }
}
