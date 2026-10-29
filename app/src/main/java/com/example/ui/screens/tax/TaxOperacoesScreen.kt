package com.example.ui.screens.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.components.CryptoComboBox
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.ui.theme.BitcoinGold
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
import com.example.viewmodel.Screen

@Composable
fun TaxOperacoesScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val carteiras by viewModel.carteiras.collectAsState()
    val lancamentos by viewModel.lancamentos.collectAsState()
    var selectedFilter by remember { mutableStateOf("Todas") }
    var selectedCarteiraFilter by remember { mutableStateOf("Todas as Carteiras") }
    var showAddDialog by remember { mutableStateOf(false) }
    var txToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredList = remember(transactions, selectedFilter, selectedCarteiraFilter) {
        transactions.filter { tx ->
            val matchType = when (selectedFilter) {
                "Compras" -> tx.type.uppercase() == "COMPRA"
                "Vendas" -> tx.type.uppercase() == "VENDA"
                "Permutas" -> tx.type.uppercase() == "PERMUTA"
                "Rendimentos" -> tx.type.uppercase() == "RENDIMENTO"
                else -> true
            }
            val matchCarteira = if (selectedCarteiraFilter == "Todas as Carteiras") {
                true
            } else {
                tx.exchange.equals(selectedCarteiraFilter, ignoreCase = true)
            }
            matchType && matchCarteira
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lançamentos & Extrato",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Histórico contábil completo de operações por carteira",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Configurações & Base button (moved from header as requested)
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Configuracoes) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CryptoDarkSurfaceVariant)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações e Base de Dados",
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Filtros tipo ComboBox
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val carteiraFilters = listOf("Todas as Carteiras") + carteiras.map { it.nome }
                    CryptoComboBox(
                        label = "Carteira",
                        selectedOption = selectedCarteiraFilter,
                        options = carteiraFilters,
                        onOptionSelected = { selectedCarteiraFilter = it },
                        modifier = Modifier.weight(1f),
                        testTag = "filter_carteira_combo"
                    )

                    CryptoComboBox(
                        label = "Tipo de Operação",
                        selectedOption = selectedFilter,
                        options = listOf("Todas", "Compras", "Vendas", "Permutas", "Rendimentos"),
                        onOptionSelected = { selectedFilter = it },
                        modifier = Modifier.weight(1f),
                        testTag = "filter_type_combo"
                    )
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📑", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma operação encontrada para o filtro '$selectedFilter'",
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Lançar Nova Operação Manual", color = CryptoDarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { tx ->
                    TransactionItemRow(
                        tx = tx,
                        onEdit = { txToEdit = tx },
                        onDelete = { txToDelete = tx }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_manual_tx_fab"),
            containerColor = CyanNeon,
            contentColor = CryptoDarkBackground
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Operação")
        }
    }

    // Modal para Adicionar Lançamento Manual
    if (showAddDialog) {
        AddManualTransactionDialog(
            carteiras = carteiras,
            onDismiss = { showAddDialog = false },
            onConfirm = { lancamento ->
                viewModel.addLancamento(lancamento)
                showAddDialog = false
            }
        )
    }

    // Modal para Editar Lançamento Manual
    if (txToEdit != null) {
        val matchingLancamento = lancamentos.find { it.id == txToEdit!!.id }
        AddManualTransactionDialog(
            carteiras = carteiras,
            transactionToEdit = txToEdit,
            lancamentoToEdit = matchingLancamento,
            onDismiss = { txToEdit = null },
            onConfirm = { lancamento ->
                viewModel.updateLancamento(lancamento)
                txToEdit = null
            }
        )
    }

    // Modal de Confirmação antes de Excluir Lançamento
    if (txToDelete != null) {
        val tx = txToDelete!!
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CryptoRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmar Exclusão",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Deseja realmente excluir a operação de ${tx.type.uppercase()} ${tx.amount} ${tx.coinSymbol} na carteira '${tx.exchange}'?\n\nEsta ação recalculará seus saldos de estoque e apuração fiscal.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx.id)
                        viewModel.deleteLancamento(tx.id)
                        txToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRed)
                ) {
                    Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CryptoDarkSurface
        )
    }
}

@Composable
fun TransactionItemRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBuy = tx.type.uppercase() == "COMPRA" || tx.type.uppercase() == "RENDIMENTO"
    val isSell = tx.type.uppercase() == "VENDA"
    val isSwap = tx.type.uppercase() == "PERMUTA"

    val typeColor = when {
        isBuy -> CryptoGreen
        isSell -> CryptoRed
        else -> CyanNeon
    }
    val typeBg = when {
        isBuy -> CryptoGreenBg
        isSell -> CryptoRedBg
        else -> CyanNeon.copy(alpha = 0.15f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CryptoDarkSurface)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type Icon Box
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(typeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isBuy -> Icons.Default.ArrowDownward
                    isSell -> Icons.Default.ArrowUpward
                    isSwap -> Icons.Default.SwapHoriz
                    else -> Icons.Default.MonetizationOn
                },
                contentDescription = null,
                tint = typeColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Center: Coin & Details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${tx.type.uppercase()} ${tx.coinSymbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                // Wallet badge: maxLines=1 with Ellipsis so mobile never wraps awkwardly
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CryptoDarkSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tx.exchange,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${tx.dateFormatted} • Qtd: ${tx.amount} ${tx.coinSymbol}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (tx.unitPriceBrl > 0) {
                Text(
                    text = "Cotação: R$ ${String.format("%,.2f", tx.unitPriceBrl)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right: Total Value
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "R$ ${String.format("%,.2f", tx.totalBrl)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isBuy) CryptoGreen else if (isSell) CryptoRed else TextPrimary,
                maxLines = 1
            )
            if (tx.feeBrl > 0) {
                Text(
                    text = "Taxa: R$ ${String.format("%.2f", tx.feeBrl)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Edit & Delete Action Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar operação",
                    tint = CyanNeon,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Excluir operação",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
