package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import com.example.data.backup.BackupImportResult
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Coin
import com.example.model.Currency
import com.example.ui.components.PortfolioAllocationChart
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenBg
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoRedBg
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CryptoViewModel
import com.example.viewmodel.PortfolioHoldingUi
import com.example.viewmodel.Screen

@Composable
fun PortfolioScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val allCoins by viewModel.coins.collectAsState()

    var showCoinSelectorDialog by remember { mutableStateOf(false) }
    var selectedCoinForAdd by remember { mutableStateOf<Coin?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonContent by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<BackupImportResult?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minha Carteira",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Acompanhamento de Lucro & Prejuízo em tempo real",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    // Import and Export Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .testTag("import_portfolio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "Importar Carteira",
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.exportPortfolio { json ->
                                    exportJsonContent = json
                                    showExportDialog = true
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .testTag("export_portfolio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Exportar Carteira",
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Portfolio Balance Card
            item {
                val isProfit = portfolioSummary.totalProfitLossUsd >= 0
                val pnlColor = if (isProfit) CryptoGreen else CryptoRed
                val pnlBg = if (isProfit) CryptoGreenBg else CryptoRedBg

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    CryptoDarkSurfaceVariant,
                                    CryptoDarkSurface
                                )
                            )
                        )
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Saldo Total da Carteira",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedCurrency.format(portfolioSummary.totalValueUsd),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Profit / Loss Badge Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(pnlBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = pnlColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (isProfit) "+" else ""}${selectedCurrency.format(portfolioSummary.totalProfitLossUsd)} (${String.format("%.2f", portfolioSummary.totalProfitLossPercent)}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = pnlColor
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Investido",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = selectedCurrency.format(portfolioSummary.totalInvestedUsd),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Allocation Donut Chart
            if (portfolioSummary.items.isNotEmpty()) {
                item {
                    PortfolioAllocationChart(
                        holdings = portfolioSummary.items,
                        totalValueUsd = portfolioSummary.totalValueUsd,
                        currency = selectedCurrency
                    )
                }
            }

            // Holdings List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meus Criptoativos (${portfolioSummary.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Button(
                        onClick = { showCoinSelectorDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_holding_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CryptoDarkBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Novo Ativo",
                            fontWeight = FontWeight.Bold,
                            color = CryptoDarkBackground,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Empty Portfolio State
            if (portfolioSummary.items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CryptoDarkSurface)
                            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "💼",
                                fontSize = 42.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sua carteira está vazia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Adicione seus primeiros ativos (Bitcoin, Ethereum, Solana...) para acompanhar seu rendimento!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCoinSelectorDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_first_asset_button")
                            ) {
                                Text(
                                    text = "Adicionar Primeiro Ativo",
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoDarkBackground
                                )
                            }
                        }
                    }
                }
            } else {
                items(portfolioSummary.items, key = { it.entity.id }) { holding ->
                    HoldingRowItem(
                        holding = holding,
                        currency = selectedCurrency,
                        onDelete = { viewModel.removePortfolioItem(holding.entity.id) },
                        onClick = {
                            holding.currentCoin?.let { coin ->
                                viewModel.navigateTo(Screen.CoinDetail(coin))
                            }
                        }
                    )
                }
            }

            // Bottom spacing for navigation
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCoinSelectorDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("portfolio_fab_add"),
            containerColor = CyanNeon,
            contentColor = CryptoDarkBackground
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar Ativo"
            )
        }
    }

    // Modal to choose which coin to add
    if (showCoinSelectorDialog) {
        SelectCoinDialog(
            coins = allCoins,
            onDismiss = { showCoinSelectorDialog = false },
            onSelectCoin = { coin ->
                selectedCoinForAdd = coin
                showCoinSelectorDialog = false
            }
        )
    }

    // Modal to input quantity and price
    if (selectedCoinForAdd != null) {
        AddTransactionDialog(
            coin = selectedCoinForAdd!!,
            selectedCurrency = selectedCurrency,
            onDismiss = { selectedCoinForAdd = null },
            onConfirm = { amount, buyPriceUsd ->
                viewModel.addPortfolioTransaction(selectedCoinForAdd!!, amount, buyPriceUsd)
                selectedCoinForAdd = null
            }
        )
    }

    // Modal to export portfolio to file / share / copy JSON
    if (showExportDialog) {
        ExportPortfolioDialog(
            jsonContent = exportJsonContent,
            holdingsCount = portfolioSummary.items.size,
            onDismiss = { showExportDialog = false }
        )
    }

    // Modal to import portfolio from file / paste text
    if (showImportDialog) {
        ImportPortfolioDialog(
            onDismiss = { showImportDialog = false },
            onImportText = { rawContent, merge ->
                viewModel.importPortfolio(rawContent, merge) { result ->
                    showImportDialog = false
                    importResult = result
                }
            }
        )
    }

    // Modal showing import result / feedback
    if (importResult != null) {
        BackupResultDialog(
            result = importResult!!,
            onDismiss = { importResult = null }
        )
    }
}

@Composable
fun HoldingRowItem(
    holding: PortfolioHoldingUi,
    currency: Currency,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProfit = holding.profitLossUsd >= 0
    val pnlColor = if (isProfit) CryptoGreen else CryptoRed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CryptoDarkSurface)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coin Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CryptoDarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!holding.entity.coinImage.isNullOrBlank()) {
                AsyncImage(
                    model = holding.entity.coinImage,
                    contentDescription = holding.entity.coinName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = holding.entity.coinSymbol.take(2),
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name & Quantity
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = holding.entity.coinName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${holding.entity.amount} ${holding.entity.coinSymbol}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "Preço médio: ${currency.format(holding.entity.buyPriceUsd)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        // Current Value & P&L
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1.2f)
        ) {
            Text(
                text = currency.format(holding.currentTotalValueUsd),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${if (isProfit) "+" else ""}${currency.format(holding.profitLossUsd)} (${String.format("%.2f", holding.profitLossPercent)}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = pnlColor,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Remover ativo",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SelectCoinDialog(
    coins: List<Coin>,
    onDismiss: () -> Unit,
    onSelectCoin: (Coin) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoDarkSurface,
        title = {
            Text(
                text = "Selecione o Criptoativo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(coins) { coin ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CryptoDarkSurfaceVariant)
                            .clickable { onSelectCoin(coin) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(CryptoDarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!coin.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = coin.image,
                                    contentDescription = coin.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = coin.symbol.take(2).uppercase(),
                                    color = CyanNeon,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = coin.name,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = coin.symbol.uppercase(),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CryptoDarkSurfaceVariant)
            ) {
                Text("Fechar", color = TextSecondary)
            }
        }
    )
}
