package com.example.ui.screens.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CarteiraEntity
import com.example.data.local.LancamentoEntity
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

    val moedasCotacao = listOf("BRL", "USDT", "USD")
    val moedasTaxa = listOf("BRL", "USDT", "BNB", "ETH", "BTC")

    val defaultCarteira = carteiras.find { it.id == preSelectedCarteiraId } ?: carteiras.firstOrNull()

    var selectedCarteira by remember { mutableStateOf<CarteiraEntity?>(defaultCarteira) }
    var selectedType by remember { mutableStateOf("COMPRA") }
    var symbolText by remember { mutableStateOf("BTC") }
    var nameText by remember { mutableStateOf("Bitcoin") }
    var amountText by remember { mutableStateOf("") }
    var unitPriceText by remember { mutableStateOf("") }
    var totalText by remember { mutableStateOf("") }
    var moedaCotacao by remember { mutableStateOf("BRL") }

    // Taxa / Fee
    var feeAmountText by remember { mutableStateOf("0") }
    var moedaTaxa by remember { mutableStateOf("BRL") }
    var feeValueBrlText by remember { mutableStateOf("0") }

    // Permuta (Ativo de Destino)
    var targetSymbolText by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var targetUnitPriceText by remember { mutableStateOf("") }

    // Transferência (Carteira de Destino)
    var selectedCarteiraDestino by remember {
        mutableStateOf<CarteiraEntity?>(carteiras.firstOrNull { it.id != selectedCarteira?.id })
    }

    // Hash da transação & Observações
    var txHashText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    // Data e Hora
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    var dateText by remember { mutableStateOf(sdf.format(Date())) }

    var carteiraMenuExpanded by remember { mutableStateOf(false) }
    var carteiraDestinoMenuExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lançar Operação (CriptoFull)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

                // 2. Tipo de Operação
                Column {
                    Text(
                        text = "Tipo de Operação:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(tiposDisponiveis) { t ->
                            val isSelected = selectedType == t
                            val chipColor = when (t) {
                                "COMPRA", "RENDIMENTO", "DEPOSITO", "AIRDROP" -> CryptoGreen
                                "VENDA", "SAQUE" -> CryptoRed
                                "PERMUTA" -> BitcoinGold
                                else -> CyanNeon
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = t },
                                label = { Text(t, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                                    selectedLabelColor = chipColor,
                                    containerColor = CryptoDarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) chipColor else CryptoCardBorder
                                )
                            )
                        }
                    }
                }

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
                                .background(CyanNeon.copy(alpha = 0.15f))
                                .clickable { dateText = sdf.format(Date()) }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agora", color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

                // 4. Ativo Principal: Símbolo e Nome
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = symbolText,
                        onValueChange = {
                            symbolText = it.uppercase()
                            if (symbolText == "BTC") nameText = "Bitcoin"
                            else if (symbolText == "ETH") nameText = "Ethereum"
                            else if (symbolText == "SOL") nameText = "Solana"
                            else if (symbolText == "USDT") nameText = "Tether USD"
                        },
                        label = { Text("Ativo (Símbolo) *") },
                        placeholder = { Text("BTC") },
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
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Nome da Moeda") },
                        placeholder = { Text("Bitcoin") },
                        modifier = Modifier.weight(1.2f),
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

                // 5. Quantidade e Moeda de Cotação
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.replace(',', '.')
                            val a = amountText.toDoubleOrNull() ?: 0.0
                            val p = unitPriceText.toDoubleOrNull() ?: 0.0
                            if (a > 0 && p > 0) {
                                totalText = String.format(Locale.US, "%.2f", a * p)
                            }
                        },
                        label = { Text("Quantidade *") },
                        placeholder = { Text("0.05") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Moeda Par", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            moedasCotacao.forEach { m ->
                                val isSelected = moedaCotacao == m
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CyanNeon.copy(alpha = 0.2f) else CryptoDarkSurfaceVariant)
                                        .border(1.dp, if (isSelected) CyanNeon else CryptoCardBorder, RoundedCornerShape(8.dp))
                                        .clickable { moedaCotacao = m }
                                        .padding(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = m,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyanNeon else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Preço Unitário e Valor Total (Cálculo bidirecional)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = {
                            unitPriceText = it.replace(',', '.')
                            val a = amountText.toDoubleOrNull() ?: 0.0
                            val p = unitPriceText.toDoubleOrNull() ?: 0.0
                            if (a > 0 && p > 0) {
                                totalText = String.format(Locale.US, "%.2f", a * p)
                            }
                        },
                        label = { Text("Preço Unit. ($moedaCotacao) *") },
                        placeholder = { Text("385000.00") },
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
                        value = totalText,
                        onValueChange = {
                            totalText = it.replace(',', '.')
                            val t = totalText.toDoubleOrNull() ?: 0.0
                            val a = amountText.toDoubleOrNull() ?: 0.0
                            if (a > 0 && t > 0) {
                                unitPriceText = String.format(Locale.US, "%.2f", t / a)
                            }
                        },
                        label = { Text("Valor Total (R$) *") },
                        placeholder = { Text("19250.00") },
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

                // 7. Taxas da Operação (Fee)
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
                                onValueChange = { targetSymbolText = it.uppercase() },
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
                                placeholder = { Text("1.5") },
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BitcoinGold.copy(alpha = 0.08f))
                            .border(1.dp, BitcoinGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Carteira de Destino da Transferência:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BitcoinGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CryptoDarkSurfaceVariant)
                                    .clickable { carteiraDestinoMenuExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCarteiraDestino?.nome ?: "Selecione Carteira de Destino",
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BitcoinGold, modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = carteiraDestinoMenuExpanded,
                                onDismissRequest = { carteiraDestinoMenuExpanded = false },
                                modifier = Modifier.background(CryptoDarkSurfaceVariant)
                            ) {
                                carteiras.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.nome} (${c.corretora})", color = TextPrimary) },
                                        onClick = {
                                            selectedCarteiraDestino = c
                                            carteiraDestinoMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 10. TxHash & Observações
                OutlinedTextField(
                    value = txHashText,
                    onValueChange = { txHashText = it },
                    label = { Text("TxHash / ID da Ordem (Opcional)") },
                    placeholder = { Text("0x... ou ID da transação") },
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

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observações / Notas (Opcional)") },
                    placeholder = { Text("Ex: Aporte mensal, lucro realizado, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = androidx.compose.ui.graphics.Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val total = totalText.toDoubleOrNull()
                    val unitPrice = unitPriceText.toDoubleOrNull() ?: if (amount != null && total != null && amount > 0) total / amount else 0.0
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

                    val carteiraNome = selectedCarteira?.nome ?: "Binance"
                    val carteiraId = selectedCarteira?.id

                    val lancamento = LancamentoEntity(
                        carteiraId = carteiraId,
                        carteiraNome = carteiraNome,
                        tipoOperacao = selectedType,
                        dataHora = dateText,
                        timestamp = System.currentTimeMillis(),
                        ativoPrincipal = symbolText.uppercase().trim(),
                        nomeAtivo = nameText.ifBlank { symbolText.uppercase() }.trim(),
                        quantidade = amount,
                        precoUnitario = unitPrice,
                        valorTotal = total,
                        moedaCotacao = moedaCotacao,
                        taxa = feeAmount,
                        moedaTaxa = moedaTaxa,
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
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Salvar Operação", fontWeight = FontWeight.Bold, color = CryptoDarkBackground)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancelar")
            }
        }
    )
}
