package com.example.ui.screens.tax

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CarteiraEntity
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CryptoViewModel
import com.example.viewmodel.Screen

@Composable
fun TaxCarteirasScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val carteiras by viewModel.carteiras.collectAsState()
    val lancamentos by viewModel.lancamentos.collectAsState()

    var selectedFilter by remember { mutableStateOf("Todas") }
    var showAddDialog by remember { mutableStateOf(false) }
    var carteiraToEdit by remember { mutableStateOf<CarteiraEntity?>(null) }
    var carteiraToDelete by remember { mutableStateOf<CarteiraEntity?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                    if (!content.isNullOrBlank()) {
                        viewModel.importPortfolio(content, merge = false) { res ->
                            backupStatusMessage = res.message
                            Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    val err = "Erro ao ler arquivo: ${e.localizedMessage}"
                    backupStatusMessage = err
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val filteredCarteiras = remember(carteiras, selectedFilter) {
        when (selectedFilter) {
            "Exchanges" -> carteiras.filter { it.tipo.uppercase() == "EXCHANGE" }
            "Software" -> carteiras.filter { it.tipo.uppercase() == "HOT_WALLET" }
            "Hardware" -> carteiras.filter { it.tipo.uppercase() == "COLD_WALLET" }
            else -> carteiras
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Minhas Carteiras",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cadastro e controle de corretoras e carteiras de custódia",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CryptoDarkBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nova Carteira",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoDarkBackground
                        )
                    }
                }
            }

            // Summary Info Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CryptoDarkSurface)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Cadastradas",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${carteiras.size} carteiras",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        val exchangeCount = carteiras.count { it.tipo.uppercase() == "EXCHANGE" }
                        Text(
                            text = "Corretoras",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "$exchangeCount exchanges",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BitcoinGold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        val selfCustodyCount = carteiras.count { it.tipo.uppercase() != "EXCHANGE" }
                        Text(
                            text = "Auto-Custódia",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "$selfCustodyCount carteiras",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGreen
                        )
                    }
                }
            }

            // Export / Import CriptoFull Backup Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.exportPortfolio { json ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "Backup CriptoFull")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Exportar Dados CriptoFull (JSON)"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            openBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BitcoinGold)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(listOf("Todas", "Exchanges", "Software", "Hardware")) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                selectedLabelColor = CyanNeon,
                                containerColor = CryptoDarkSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyanNeon else CryptoCardBorder
                            )
                        )
                    }
                }
            }

            // Carteiras List
            if (filteredCarteiras.isEmpty()) {
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
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Nenhuma carteira encontrada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cadastre suas corretoras e carteiras para organizar seus lançamentos contábeis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cadastrar Primeira Carteira", color = CryptoDarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredCarteiras, key = { it.id }) { carteira ->
                    val txCount = lancamentos.count { it.carteiraNome.equals(carteira.nome, ignoreCase = true) || it.carteiraId == carteira.id }
                    CarteiraCardItem(
                        carteira = carteira,
                        transactionCount = txCount,
                        onEdit = { carteiraToEdit = carteira },
                        onDelete = { carteiraToDelete = carteira }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Modal: Criar ou Editar Carteira
    if (showAddDialog || carteiraToEdit != null) {
        AddEditCarteiraDialog(
            carteiraToEdit = carteiraToEdit,
            onDismiss = {
                showAddDialog = false
                carteiraToEdit = null
            },
            onConfirm = { nome, tipo, corretora, endereco, cor, obs ->
                if (carteiraToEdit != null) {
                    viewModel.updateCarteira(
                        carteiraToEdit!!.copy(
                            nome = nome,
                            tipo = tipo,
                            corretora = corretora,
                            endereco = endereco,
                            cor = cor,
                            observacoes = obs
                        )
                    ) {
                        Toast.makeText(context, "Carteira atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.addCarteira(
                        nome = nome,
                        tipo = tipo,
                        corretora = corretora,
                        endereco = endereco,
                        cor = cor,
                        observacoes = obs
                    ) {
                        Toast.makeText(context, "Carteira cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                }
                showAddDialog = false
                carteiraToEdit = null
            }
        )
    }

    // Dialog: Confirmar Exclusão
    if (carteiraToDelete != null) {
        val c = carteiraToDelete!!
        AlertDialog(
            onDismissRequest = { carteiraToDelete = null },
            containerColor = CryptoDarkSurface,
            title = {
                Text(text = "Excluir Carteira", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Deseja realmente excluir a carteira '${c.nome}'? As operações vinculadas a ela não serão apagadas.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCarteira(c.id)
                        carteiraToDelete = null
                        Toast.makeText(context, "Carteira excluída.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRed)
                ) {
                    Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { carteiraToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (backupStatusMessage != null) {
        AlertDialog(
            onDismissRequest = { backupStatusMessage = null },
            containerColor = CryptoDarkSurface,
            title = {
                Text(text = "Restauração / Backup", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = backupStatusMessage!!, color = TextSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { backupStatusMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
                ) {
                    Text("OK", color = CryptoDarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun CarteiraCardItem(
    carteira: CarteiraEntity,
    transactionCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val corBadge = try {
        Color(android.graphics.Color.parseColor(carteira.cor))
    } catch (e: Exception) {
        CyanNeon
    }

    val tipoLabel = when (carteira.tipo.uppercase()) {
        "EXCHANGE" -> "Corretora"
        "HOT_WALLET" -> "Software / Hot"
        "COLD_WALLET" -> "Hardware / Fria"
        else -> "Carteira"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CryptoDarkSurface)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("carteira_card_${carteira.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dot de cor da carteira
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(corBadge)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = carteira.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(corBadge.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$tipoLabel • ${carteira.corretora}",
                                style = MaterialTheme.typography.labelSmall,
                                color = corBadge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Edit & Delete Buttons
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir",
                        tint = CryptoRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (!carteira.endereco.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoDarkSurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = carteira.endereco,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        if (!carteira.observacoes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = carteira.observacoes,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Operations counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CryptoDarkSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lançamentos vinculados:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "$transactionCount operações",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
