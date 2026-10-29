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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CarteiraEntity
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddEditCarteiraDialog(
    carteiraToEdit: CarteiraEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        nome: String,
        tipo: String,
        corretora: String,
        endereco: String?,
        cor: String,
        observacoes: String?
    ) -> Unit
) {
    var nome by remember { mutableStateOf(carteiraToEdit?.nome ?: "") }
    var tipo by remember { mutableStateOf(carteiraToEdit?.tipo ?: "EXCHANGE") }
    var corretora by remember { mutableStateOf(carteiraToEdit?.corretora ?: "Binance") }
    var endereco by remember { mutableStateOf(carteiraToEdit?.endereco ?: "") }
    var corSelecionada by remember { mutableStateOf(carteiraToEdit?.cor ?: "#00E5FF") }
    var observacoes by remember { mutableStateOf(carteiraToEdit?.observacoes ?: "") }
    var erroNome by remember { mutableStateOf<String?>(null) }

    val tiposDisponiveis = listOf(
        Pair("EXCHANGE", "Corretora / Exchange"),
        Pair("HOT_WALLET", "Carteira Quente (Software)"),
        Pair("COLD_WALLET", "Carteira Fria (Hardware)"),
        Pair("OUTRA", "Outra Carteira")
    )

    val corretorasDisponiveis = listOf(
        "Binance",
        "Mercado Bitcoin",
        "Bybit",
        "Foxbit",
        "Coinbase",
        "Metamask",
        "Trust Wallet",
        "Phantom",
        "Ledger",
        "Trezor",
        "Outra"
    )

    val coresDisponiveis = listOf(
        "#00E5FF", // Cyan Neon
        "#F59E0B", // Bitcoin Gold
        "#10B981", // Verde Crypto
        "#8B5CF6", // Roxo DeFi
        "#EC4899", // Rosa
        "#3B82F6", // Azul Elétrico
        "#FF5A00", // Laranja MB
        "#64748B"  // Cinza Neutro
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (carteiraToEdit == null) "Cadastrar Nova Carteira" else "Editar Carteira",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nome da Carteira
                Column {
                    Text(
                        text = "Nome da Carteira / Conta *",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            erroNome = null
                        },
                        placeholder = { Text("Ex: Binance Principal, Metamask DeFi") },
                        isError = erroNome != null,
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
                    if (erroNome != null) {
                        Text(
                            text = erroNome!!,
                            color = androidx.compose.ui.graphics.Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Tipo de Carteira (ComboBox)
                val selectedTipoLabel = tiposDisponiveis.firstOrNull { it.first == tipo }?.second ?: "Corretora / Exchange"
                CryptoComboBox(
                    label = "Tipo de Custódia:",
                    selectedOption = selectedTipoLabel,
                    options = tiposDisponiveis.map { it.second },
                    onOptionSelected = { label ->
                        tipo = tiposDisponiveis.firstOrNull { it.second == label }?.first ?: "EXCHANGE"
                    },
                    testTag = "dialog_carteira_tipo_combo"
                )

                // Provedor / Corretora / Plataforma (ComboBox)
                CryptoComboBox(
                    label = "Provedor / Corretora:",
                    selectedOption = corretora,
                    options = corretorasDisponiveis,
                    onOptionSelected = { corretora = it },
                    testTag = "dialog_carteira_corretora_combo"
                )

                // Cor do Identificador
                Column {
                    Text(
                        text = "Cor de Identificação:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        coresDisponiveis.forEach { hexColor ->
                            val parsedColor = Color(android.graphics.Color.parseColor(hexColor))
                            val isSelected = corSelecionada.equals(hexColor, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .clickable { corSelecionada = hexColor }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Endereço Público / Chave / Rede (opcional)
                Column {
                    Text(
                        text = "Endereço Público / Rede (Opcional)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = endereco,
                        onValueChange = { endereco = it },
                        placeholder = { Text("0x... ou rede Bitcoin/Solana") },
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

                // Observações (opcional)
                Column {
                    Text(
                        text = "Observações / Notas",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        placeholder = { Text("Ex: Conta conjunta, carteira de staking, etc.") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isBlank()) {
                        erroNome = "Informe um nome para a carteira."
                        return@Button
                    }
                    onConfirm(
                        nome.trim(),
                        tipo,
                        corretora,
                        endereco.ifBlank { null },
                        corSelecionada,
                        observacoes.ifBlank { null }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (carteiraToEdit == null) "Salvar Carteira" else "Atualizar",
                    color = CryptoDarkBackground,
                    fontWeight = FontWeight.Bold
                )
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
