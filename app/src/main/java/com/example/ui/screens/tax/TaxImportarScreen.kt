package com.example.ui.screens.tax

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.components.CryptoComboBox
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ImportStatementResult
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CryptoViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TaxImportarScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exchangeList = listOf("Binance", "Mercado Bitcoin", "Bybit", "Foxbit", "Coinbase", "Modelo CriptoFull")
    var selectedExchange by remember { mutableStateOf("Binance") }
    var pastedContent by remember { mutableStateOf("") }
    var showPasteArea by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportStatementResult?>(null) }

    // Launcher to pick a statement CSV or JSON file from Android storage
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                    if (!content.isNullOrBlank()) {
                        if (content.contains("\"lancamentos\"") || content.contains("\"carteiras\"")) {
                            viewModel.importPortfolio(content, merge = false) { bRes ->
                                importResult = ImportStatementResult(
                                    success = bRes.success,
                                    exchangeName = "CriptoFull Backup",
                                    importedCount = bRes.importedLancamentosCount,
                                    transactions = emptyList(),
                                    message = bRes.message
                                )
                            }
                        } else {
                            viewModel.importExchangeStatement(content, selectedExchange) { res ->
                                importResult = res
                            }
                        }
                    } else {
                        Toast.makeText(context, "O arquivo selecionado está vazio.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao ler arquivo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title with Back Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoDarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Importar Extrato ou Backup",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Importe arquivos CSV de corretoras ou o arquivo JSON gerado pelo CriptoFull",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Exchange Selector ComboBox
        item {
            CryptoComboBox(
                label = "Selecione a Corretora ou Formato:",
                selectedOption = selectedExchange,
                options = exchangeList,
                onOptionSelected = { selectedExchange = it },
                leadingIcon = Icons.Default.CloudUpload,
                testTag = "import_exchange_combo"
            )
        }

        // Upload Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CryptoDarkSurface)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(18.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Arquivo de Extrato ($selectedExchange)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Formatos aceitos: Arquivo CSV de histórico de ordens/trades ou arquivo JSON de backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            openFileLauncher.launch(arrayOf("*/*", "text/*", "application/json", "application/csv"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_statement_file_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = CryptoDarkBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selecionar Arquivo do Dispositivo",
                            fontWeight = FontWeight.Bold,
                            color = CryptoDarkBackground
                        )
                    }
                }
            }
        }

        // Paste Text Alternative Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CryptoDarkSurface)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Colar Extrato em Texto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedButton(
                        onClick = { showPasteArea = !showPasteArea },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon)
                    ) {
                        Text(if (showPasteArea) "Ocultar" else "Abrir Campo", fontSize = 11.sp)
                    }
                }

                if (showPasteArea) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pastedContent,
                        onValueChange = { pastedContent = it },
                        placeholder = {
                            Text(
                                text = "Cole as linhas do CSV da corretora aqui...\nExemplo:\nData,Tipo,Moeda,Qtd,Preco,Total\n10/05/2026,Compra,BTC,0.1,340000,34000",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("paste_statement_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CryptoDarkBackground,
                            unfocusedContainerColor = CryptoDarkBackground,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (pastedContent.isNotBlank()) {
                                if (pastedContent.contains("\"lancamentos\"") || pastedContent.contains("\"carteiras\"")) {
                                    viewModel.importPortfolio(pastedContent, merge = false) { bRes ->
                                        importResult = ImportStatementResult(
                                            success = bRes.success,
                                            exchangeName = "CriptoFull Backup",
                                            importedCount = bRes.importedLancamentosCount,
                                            transactions = emptyList(),
                                            message = bRes.message
                                        )
                                    }
                                } else {
                                    viewModel.importExchangeStatement(pastedContent, selectedExchange) { res ->
                                        importResult = res
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(10.dp),
                        enabled = pastedContent.isNotBlank()
                    ) {
                        Text("Processar e Apurar Extrato / Backup", fontWeight = FontWeight.Bold, color = CryptoDarkBackground)
                    }
                }
            }
        }

        // Quick Demonstration Templates
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CryptoDarkSurfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Testar com Modelos Prontos de Corretoras:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val binanceSample = """
                                Date(UTC),Market,Type,Price,Amount,Total,Fee,Fee Coin
                                2026-05-10 14:00:00,BTCBRL,BUY,340000.00,0.15,51000.00,25.50,BRL
                                2026-05-20 18:30:00,BTCBRL,SELL,370000.00,0.10,37000.00,18.50,BRL
                            """.trimIndent()
                            viewModel.importExchangeStatement(binanceSample, "Binance") { res ->
                                importResult = res
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Exemplo Binance", fontSize = 11.sp, color = BitcoinGold)
                    }

                    OutlinedButton(
                        onClick = {
                            val mbSample = """
                                Data,Tipo,Instrumento,Quantidade,Preço,Total Bruto,Taxa
                                12/06/2026 10:15:00,Compra,ETH-BRL,3.0,17500.00,52500.00,30.00
                                25/06/2026 16:40:00,Venda,ETH-BRL,1.0,19200.00,19200.00,15.00
                            """.trimIndent()
                            viewModel.importExchangeStatement(mbSample, "Mercado Bitcoin") { res ->
                                importResult = res
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Exemplo Mercado Bitcoin", fontSize = 11.sp, color = CyanNeon)
                    }
                }
            }
        }

        // Export Backup Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CryptoDarkSurface)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exportar Carteiras & Operações",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Gere o arquivo JSON oficial do CriptoFull contendo todas as carteiras e lançamentos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.exportPortfolio { json ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "Backup CriptoFull")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Exportar Dados CriptoFull"))
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal with Import Statement Result
    if (importResult != null) {
        AlertDialog(
            onDismissRequest = { importResult = null },
            containerColor = CryptoDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (importResult!!.success) Icons.Default.CheckCircle else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (importResult!!.success) CryptoGreen else CyanNeon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (importResult!!.success) "Importação Concluída!" else "Aviso de Importação",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = importResult!!.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    if (importResult!!.success) {
                        Text(
                            text = "As compras e vendas foram consolidadas no histórico. O lucro/prejuízo e o DARF foram recalculados automaticamente pela apuração.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        importResult = null
                        viewModel.navigateTo(Screen.Apuracao)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ver Apuração Fiscal", color = CryptoDarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { importResult = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fechar")
                }
            }
        )
    }
}
