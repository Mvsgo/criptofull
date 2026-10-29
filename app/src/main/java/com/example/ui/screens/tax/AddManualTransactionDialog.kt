package com.example.ui.screens.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CarteiraEntity
import com.example.data.local.LancamentoEntity
import com.example.data.local.TransactionEntity
import com.example.ui.components.CryptoComboBox
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddManualTransactionDialog(
    carteiras: List<CarteiraEntity> = emptyList(),
    preSelectedCarteiraId: Long? = null,
    transactionToEdit: TransactionEntity? = null,
    lancamentoToEdit: LancamentoEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (LancamentoEntity) -> Unit
) {
    val tiposDisponiveis = listOf(
        "COMPRA",
        "VENDA",
        "PERMUTA",
        "TRANSFERENCIA",
        "RENDIMENTO",
        "DEPOSITO",
        "SAQUE",
        "AIRDROP"
    )

    val defaultCarteira = if (transactionToEdit != null) {
        carteiras.find { it.nome.equals(transactionToEdit.exchange, ignoreCase = true) } ?: carteiras.firstOrNull()
    } else {
        carteiras.find { it.id == preSelectedCarteiraId } ?: carteiras.firstOrNull()
    }

    var selectedCarteira by remember { mutableStateOf<CarteiraEntity?>(defaultCarteira) }
    var selectedType by remember {
        mutableStateOf(lancamentoToEdit?.tipoOperacao ?: transactionToEdit?.type?.uppercase() ?: "COMPRA")
    }
    var symbolText by remember {
        mutableStateOf(lancamentoToEdit?.ativoPrincipal ?: transactionToEdit?.coinSymbol ?: "BTC")
    }
    var amountText by remember {
        mutableStateOf(
            if (lancamentoToEdit != null && lancamentoToEdit.quantidade > 0) {
                lancamentoToEdit.quantidade.toString()
            } else if (transactionToEdit != null && transactionToEdit.amount > 0) {
                transactionToEdit.amount.toString()
            } else ""
        )
    }

    // Moeda Par (Não travada em apenas 3 moedas; pode ser qualquer par como BTC, BNB, USDT, etc.)
    var moedaCotacao by remember {
        mutableStateOf(lancamentoToEdit?.moedaCotacao ?: "BRL")
    }

    // Cotação da moeda par em Reais (ex: 1 BTC = R$ 385.000,00 ou 1 BNB = R$ 3.400,00 ou 1 USDT = R$ 5,70)
    var cotacaoParEmBrlText by remember {
        mutableStateOf(
            if (moedaCotacao == "BRL") "1.0"
            else when (moedaCotacao) {
                "USDT", "USDC", "USD" -> "5.70"
                "BTC" -> "385000.00"
                "BNB" -> "3400.00"
                "ETH" -> "18500.00"
                else -> ""
            }
        )
    }

    var unitPriceText by remember {
        mutableStateOf(
            if (lancamentoToEdit != null && lancamentoToEdit.precoUnitario > 0) {
                lancamentoToEdit.precoUnitario.toString()
            } else if (transactionToEdit != null && transactionToEdit.unitPriceBrl > 0) {
                transactionToEdit.unitPriceBrl.toString()
            } else ""
        )
    }

    var totalText by remember {
        mutableStateOf(
            if (lancamentoToEdit != null && lancamentoToEdit.valorTotal > 0) {
                lancamentoToEdit.valorTotal.toString()
            } else if (transactionToEdit != null && transactionToEdit.totalBrl > 0) {
                transactionToEdit.totalBrl.toString()
            } else ""
        )
    }

    // Taxa / Fee
    var feeAmountText by remember {
        mutableStateOf(
            if (lancamentoToEdit != null && lancamentoToEdit.taxa > 0) lancamentoToEdit.taxa.toString()
            else if (transactionToEdit != null && transactionToEdit.feeBrl > 0) transactionToEdit.feeBrl.toString()
            else "0"
        )
    }
    var moedaTaxa by remember {
        mutableStateOf(lancamentoToEdit?.moedaTaxa ?: if (moedaCotacao == "BNB") "BNB" else "BRL")
    }
    var feeValueBrlText by remember {
        mutableStateOf(
            if (lancamentoToEdit != null && lancamentoToEdit.valorTaxaBrl > 0) lancamentoToEdit.valorTaxaBrl.toString()
            else if (transactionToEdit != null && transactionToEdit.feeBrl > 0) transactionToEdit.feeBrl.toString()
            else "0"
        )
    }

    // Permuta (Ativo de Destino)
    var targetSymbolText by remember {
        mutableStateOf(lancamentoToEdit?.ativoSecundario ?: transactionToEdit?.targetCoinSymbol ?: "")
    }
    var targetAmountText by remember {
        mutableStateOf(
            lancamentoToEdit?.quantidadeSecundaria?.toString()
                ?: transactionToEdit?.targetAmount?.toString()
                ?: ""
        )
    }
    var targetUnitPriceText by remember {
        mutableStateOf(lancamentoToEdit?.precoUnitarioSecundario?.toString() ?: "")
    }

    // Transferência (Carteira de Destino)
    var selectedCarteiraDestino by remember {
        mutableStateOf<CarteiraEntity?>(
            if (lancamentoToEdit?.carteiraDestinoId != null) {
                carteiras.find { it.id == lancamentoToEdit.carteiraDestinoId }
            } else {
                carteiras.firstOrNull { it.id != selectedCarteira?.id }
            }
        )
    }

    // Hash da transação & Observações
    var txHashText by remember { mutableStateOf(lancamentoToEdit?.txHash ?: "") }
    var notesText by remember {
        mutableStateOf(lancamentoToEdit?.observacoes ?: transactionToEdit?.notes ?: "")
    }

    // Data e Hora
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    var dateText by remember {
        mutableStateOf(
            lancamentoToEdit?.dataHora?.ifBlank { null }
                ?: transactionToEdit?.dateFormatted?.ifBlank { null }
                ?: sdf.format(Date())
        )
    }

    var carteiraMenuExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun recalculateTotal() {
        val a = amountText.toDoubleOrNull() ?: 0.0
        val p = unitPriceText.toDoubleOrNull() ?: 0.0
        if (a > 0 && p > 0) {
            val multBrl = if (moedaCotacao == "BRL") 1.0 else (cotacaoParEmBrlText.toDoubleOrNull() ?: 1.0)
            totalText = String.format(Locale.US, "%.2f", a * p * multBrl)
        }
    }

    fun recalculateFromTotal() {
        val t = totalText.toDoubleOrNull() ?: 0.0
        val a = amountText.toDoubleOrNull() ?: 0.0
        if (a > 0 && t > 0) {
            if (moedaCotacao == "BRL") {
                unitPriceText = String.format(Locale.US, "%.2f", t / a)
            } else {
                val p = unitPriceText.toDoubleOrNull() ?: 0.0
                if (p > 0) {
                    cotacaoParEmBrlText = String.format(Locale.US, "%.4f", t / (a * p))
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.96f)
                .widthIn(max = 840.dp),
            shape = RoundedCornerShape(16.dp),
            color = CryptoDarkSurface,
            border = BorderStroke(1.dp, CryptoCardBorder),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Cabeçalho fixo com Título, Par e Botão Fechar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (transactionToEdit != null || lancamentoToEdit != null) "Editar Operação" else "Lançar Operação",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Par: ${symbolText.ifBlank { "..." }} / ${moedaCotacao.ifBlank { "..." }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanNeon,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Área rolável responsiva com preenchimento total
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val isWide = maxWidth > 560.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                // 1. Carteira de Custódia / Origem
                Column {
                    Text(
                        text = "Carteira / Corretora de Origem *",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .clickable { carteiraMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val cor = try {
                                    Color(android.graphics.Color.parseColor(selectedCarteira?.cor ?: "#00E5FF"))
                                } catch (e: Exception) {
                                    CyanNeon
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(cor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedCarteira?.nome ?: "Selecione uma Carteira",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                if (selectedCarteira != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${selectedCarteira!!.corretora})",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                        }

                        DropdownMenu(
                            expanded = carteiraMenuExpanded,
                            onDismissRequest = { carteiraMenuExpanded = false },
                            modifier = Modifier.background(CryptoDarkSurfaceVariant)
                        ) {
                            carteiras.forEach { c ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val cColor = try {
                                                Color(android.graphics.Color.parseColor(c.cor))
                                            } catch (e: Exception) {
                                                CyanNeon
                                            }
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(cColor))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("${c.nome} (${c.corretora})", color = TextPrimary)
                                        }
                                    },
                                    onClick = {
                                        selectedCarteira = c
                                        carteiraMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Tipo de Operação (ComboBox)
                CryptoComboBox(
                    label = "Tipo de Operação *",
                    selectedOption = selectedType,
                    options = tiposDisponiveis,
                    onOptionSelected = { selectedType = it },
                    testTag = "dialog_type_combo"
                )

                // 3. Data e Hora com botão "Agora"
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Data e Hora da Operação *",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .clickable { dateText = sdf.format(Date()) }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agora", fontSize = 10.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        placeholder = { Text("dd/MM/yyyy HH:mm") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // 4. Par de Negociação: Ativo Base e Moeda Par (Cotação 100% livre)
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = symbolText,
                            onValueChange = { symbolText = it.uppercase() },
                            label = { Text("Ativo (Base) *") },
                            placeholder = { Text("Ex: ETH, SOL, BTC") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = CryptoCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = moedaCotacao,
                            onValueChange = {
                                moedaCotacao = it.uppercase()
                                if (moedaCotacao.trim() == "BRL") {
                                    cotacaoParEmBrlText = "1.0"
                                }
                                recalculateTotal()
                            },
                            label = { Text("Moeda Par (Cotação) *") },
                            placeholder = { Text("Ex: BTC, BNB, USDT, BRL") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = CryptoCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CryptoDarkSurfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Par: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${symbolText.ifBlank { "..." }} / ${moedaCotacao.ifBlank { "..." }}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Qualquer moeda livre (BTC, BNB, USDT, BRL...)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                // 5. Quantidade e Preço Unitário no Par (Nome da Moeda removido)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.replace(',', '.')
                            recalculateTotal()
                        },
                        label = { Text("Quantidade *") },
                        placeholder = { Text("0.05") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = {
                            unitPriceText = it.replace(',', '.')
                            recalculateTotal()
                        },
                        label = { Text("Preço Unit. ($moedaCotacao) *") },
                        placeholder = { Text(if (moedaCotacao == "BTC") "0.05" else if (moedaCotacao == "BNB") "0.50" else "150.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // 6. Total Fiscal em Reais e Cotação do Par
                Column {
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = {
                            totalText = it.replace(',', '.')
                            recalculateFromTotal()
                        },
                        label = { Text(if (moedaCotacao == "BRL") "Valor Total (R$) *" else "Total Fiscal em Reais (R$) *") },
                        placeholder = { Text("1500.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Se a moeda par NÃO for BRL (ex: BTC, BNB, USDT, ETH, etc.), exibir conversão e cotação para apuração fiscal
                    if (moedaCotacao.isNotBlank() && moedaCotacao != "BRL") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CryptoDarkSurfaceVariant.copy(alpha = 0.6f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = cotacaoParEmBrlText,
                                onValueChange = {
                                    cotacaoParEmBrlText = it.replace(',', '.')
                                    recalculateTotal()
                                },
                                label = { Text("Cotação 1 $moedaCotacao em R$ *") },
                                placeholder = {
                                    Text(
                                        when (moedaCotacao) {
                                            "BTC" -> "385000.00"
                                            "BNB" -> "3400.00"
                                            "ETH" -> "18500.00"
                                            else -> "5.70"
                                        }
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BitcoinGold,
                                    unfocusedBorderColor = CryptoCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            // Resumo no Par
                            val a = amountText.toDoubleOrNull() ?: 0.0
                            val p = unitPriceText.toDoubleOrNull() ?: 0.0
                            val totalNoPar = a * p
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Total no Par:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = String.format(Locale.US, "%.6f %s", totalNoPar, moedaCotacao),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanNeon,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 7. Taxas da Operação (Fee) com Moeda da Taxa configurável (BNB, BTC, BRL, USDT...)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CryptoDarkSurfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Taxa da Corretora / Gas (Dedução Fiscal):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BitcoinGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = feeAmountText,
                            onValueChange = { feeAmountText = it.replace(',', '.') },
                            label = { Text("Qtd. Taxa") },
                            placeholder = { Text("0.0001") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BitcoinGold,
                                unfocusedBorderColor = CryptoCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = moedaTaxa,
                            onValueChange = { moedaTaxa = it.uppercase().trim() },
                            label = { Text("Moeda Taxa") },
                            placeholder = { Text("BNB, BRL, BTC") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BitcoinGold,
                                unfocusedBorderColor = CryptoCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = feeValueBrlText,
                            onValueChange = { feeValueBrlText = it.replace(',', '.') },
                            label = { Text("Taxa em R$") },
                            placeholder = { Text("15.50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BitcoinGold,
                                unfocusedBorderColor = CryptoCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // 8. Se PERMUTA: campos do ativo recebido
                if (selectedType == "PERMUTA") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanNeon.copy(alpha = 0.08f))
                            .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Ativo Recebido na Permuta (Troca):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = targetSymbolText,
                                onValueChange = { targetSymbolText = it.uppercase().trim() },
                                label = { Text("Ativo Recebido") },
                                placeholder = { Text("ETH") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = targetAmountText,
                                onValueChange = { targetAmountText = it.replace(',', '.') },
                                label = { Text("Qtd. Recebida") },
                                placeholder = { Text("1.2") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // 9. Se TRANSFERÊNCIA: Carteira de Destino
                if (selectedType == "TRANSFERENCIA") {
                    Column {
                        Text(
                            text = "Carteira de Destino:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val outrasCarteiras = carteiras.filter { it.id != selectedCarteira?.id }
                        if (outrasCarteiras.isEmpty()) {
                            Text(
                                text = "Cadastre outra carteira para realizar transferências entre custódias.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        } else {
                            var destMenuExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CryptoDarkSurfaceVariant)
                                        .clickable { destMenuExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCarteiraDestino?.nome ?: "Selecione a carteira de destino",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedCarteiraDestino != null) TextPrimary else TextMuted,
                                        fontSize = 13.sp
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                                }

                                DropdownMenu(
                                    expanded = destMenuExpanded,
                                    onDismissRequest = { destMenuExpanded = false },
                                    modifier = Modifier.background(CryptoDarkSurfaceVariant)
                                ) {
                                    outrasCarteiras.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.nome} (${c.corretora})", color = TextPrimary) },
                                            onClick = {
                                                selectedCarteiraDestino = c
                                                destMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 10. Hash da Transação & Observações
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = txHashText,
                        onValueChange = { txHashText = it },
                        label = { Text("Hash da Transação (TxID / Ordem)") },
                        placeholder = { Text("0x123abc...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Observações Fiscais / Notas") },
                        placeholder = { Text("Ex: Compra com saldo BNB, ordem limite...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = CryptoRed, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rodapé fixo sempre visível com Botões de Ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val total = totalText.toDoubleOrNull()
                    val unitPricePar = unitPriceText.toDoubleOrNull() ?: if (amount != null && total != null && amount > 0) total / amount else 0.0
                    val feeAmount = feeAmountText.toDoubleOrNull() ?: 0.0
                    val feeValueBrl = feeValueBrlText.toDoubleOrNull() ?: 0.0

                    if (symbolText.isBlank()) {
                        errorMsg = "Informe o símbolo do ativo (ex: BTC, ETH)."
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMsg = "Informe uma quantidade válida."
                        return@Button
                    }
                    if (total == null || total <= 0.0) {
                        errorMsg = "Informe o valor total da operação."
                        return@Button
                    }
                    if (moedaCotacao.isBlank()) {
                        errorMsg = "Informe a moeda par da operação (ex: BRL, BTC, BNB, USDT)."
                        return@Button
                    }

                    val carteiraNome = selectedCarteira?.nome ?: "Binance"
                    val carteiraId = selectedCarteira?.id

                    // Para apuração fiscal em Reais:
                    val unitPriceBrl = if (amount > 0) total / amount else unitPricePar

                    val lancamento = LancamentoEntity(
                        id = lancamentoToEdit?.id ?: transactionToEdit?.id ?: 0,
                        carteiraId = carteiraId,
                        carteiraNome = carteiraNome,
                        tipoOperacao = selectedType,
                        dataHora = dateText,
                        timestamp = lancamentoToEdit?.timestamp ?: transactionToEdit?.timestamp ?: System.currentTimeMillis(),
                        ativoPrincipal = symbolText.uppercase().trim(),
                        nomeAtivo = symbolText.uppercase().trim(),
                        quantidade = amount,
                        precoUnitario = unitPriceBrl,
                        valorTotal = total,
                        moedaCotacao = moedaCotacao.uppercase().trim(),
                        taxa = feeAmount,
                        moedaTaxa = moedaTaxa.uppercase().trim(),
                        valorTaxaBrl = feeValueBrl,
                        ativoSecundario = targetSymbolText.ifBlank { null }?.uppercase()?.trim(),
                        quantidadeSecundaria = targetAmountText.toDoubleOrNull(),
                        precoUnitarioSecundario = targetUnitPriceText.toDoubleOrNull(),
                        carteiraDestinoId = selectedCarteiraDestino?.id,
                        carteiraDestinoNome = selectedCarteiraDestino?.nome,
                        txHash = txHashText.ifBlank { null }?.trim(),
                        observacoes = notesText.ifBlank { null }?.trim()
                    )

                    onConfirm(lancamento)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
            ) {
                Text(
                    text = if (transactionToEdit != null || lancamentoToEdit != null) "Atualizar Operação" else "Salvar Operação",
                    fontWeight = FontWeight.Bold,
                    color = CryptoDarkBackground
                )
            }
        }
    }
}
}
}
