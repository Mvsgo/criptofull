package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CoinDetailScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.tax.TaxApuracaoScreen
import com.example.ui.screens.tax.TaxCarteirasScreen
import com.example.ui.screens.tax.TaxConfiguracoesScreen
import com.example.ui.screens.tax.TaxEstoqueScreen
import com.example.ui.screens.tax.TaxImportarScreen
import com.example.ui.screens.tax.TaxOperacoesScreen
import com.example.ui.screens.tax.TaxRelatoriosScreen
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.viewmodel.CryptoViewModel
import com.example.viewmodel.Screen

sealed class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Apuracao : NavItem("Apuração", Icons.Filled.Calculate, Icons.Outlined.Calculate, "nav_tax_apuracao")
    object Operacoes : NavItem("Lançamentos", Icons.Filled.ListAlt, Icons.Outlined.ListAlt, "nav_tax_operacoes")
    object Carteiras : NavItem("Carteiras", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_tax_carteiras")
    object Estoque : NavItem("Estoque", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_tax_estoque")
    object Relatorios : NavItem("Relatórios", Icons.Filled.Description, Icons.Outlined.Description, "nav_tax_relatorios")
}

class MainActivity : ComponentActivity() {
    private val viewModel: CryptoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CriptoFullApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CriptoFullApp(viewModel: CryptoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val navItems = listOf(
        NavItem.Apuracao,
        NavItem.Operacoes,
        NavItem.Carteiras,
        NavItem.Estoque,
        NavItem.Relatorios
    )

    val currentTab = when (currentScreen) {
        is Screen.Apuracao -> NavItem.Apuracao
        is Screen.Operacoes -> NavItem.Operacoes
        is Screen.Carteiras -> NavItem.Carteiras
        is Screen.Estoque -> NavItem.Estoque
        is Screen.Relatorios -> NavItem.Relatorios
        is Screen.Importar, is Screen.Configuracoes -> NavItem.Operacoes
        is Screen.Mercado, is Screen.CoinDetail -> NavItem.Apuracao
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = CryptoDarkSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentTab == item
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            when (item) {
                                NavItem.Apuracao -> viewModel.navigateTo(Screen.Apuracao)
                                NavItem.Operacoes -> viewModel.navigateTo(Screen.Operacoes)
                                NavItem.Carteiras -> viewModel.navigateTo(Screen.Carteiras)
                                NavItem.Estoque -> viewModel.navigateTo(Screen.Estoque)
                                NavItem.Relatorios -> viewModel.navigateTo(Screen.Relatorios)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CryptoDarkBackground,
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CryptoDarkBackground)
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Apuracao -> {
                    TaxApuracaoScreen(viewModel = viewModel)
                }
                is Screen.Operacoes -> {
                    TaxOperacoesScreen(viewModel = viewModel)
                }
                is Screen.Carteiras -> {
                    TaxCarteirasScreen(viewModel = viewModel)
                }
                is Screen.Estoque -> {
                    TaxEstoqueScreen(viewModel = viewModel)
                }
                is Screen.Relatorios -> {
                    TaxRelatoriosScreen(viewModel = viewModel)
                }
                is Screen.Importar -> {
                    BackHandler { viewModel.navigateBack() }
                    TaxImportarScreen(viewModel = viewModel)
                }
                is Screen.Configuracoes -> {
                    BackHandler { viewModel.navigateBack() }
                    TaxConfiguracoesScreen(viewModel = viewModel)
                }
                is Screen.Mercado -> {
                    BackHandler { viewModel.navigateBack() }
                    MarketScreen(viewModel = viewModel)
                }
                is Screen.CoinDetail -> {
                    BackHandler { viewModel.navigateBack() }
                    CoinDetailScreen(coin = screen.coin, viewModel = viewModel)
                }
            }
        }
    }
}
