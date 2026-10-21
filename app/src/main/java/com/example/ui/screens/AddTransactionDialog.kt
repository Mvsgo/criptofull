package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Coin
import com.example.model.Currency
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddTransactionDialog(
    coin: Coin,
    selectedCurrency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, buyPriceUsd: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    // Pre-fill buy price with current price in chosen currency
    var buyPriceText by remember {
        mutableStateOf(String.format("%.2f", coin.currentPrice * selectedCurrency.rateFromUsd))
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoDarkSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CryptoDarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!coin.image.isNullOrBlank()) {
                        AsyncImage(
                            model = coin.image,
                            contentDescription = coin.name,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Text(
                            text = coin.symbol.take(2).uppercase(),
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Adicionar ${coin.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "À sua Carteira CriptoFull",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Quantity input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.replace(',', '.')
                        errorMessage = null
                    },
                    label = { Text("Quantidade (${coin.symbol.uppercase()})") },
                    placeholder = { Text("Ex: 0.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = CyanNeon,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                // Buy Price input in selected currency
                OutlinedTextField(
                    value = buyPriceText,
                    onValueChange = {
                        buyPriceText = it.replace(',', '.')
                        errorMessage = null
                    },
                    label = { Text("Preço de Compra (${selectedCurrency.symbol})") },
                    placeholder = { Text("Ex: ${String.format("%.2f", coin.currentPrice * selectedCurrency.rateFromUsd)}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_price_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = CyanNeon,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = androidx.compose.ui.graphics.Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Estimated total
                val amount = amountText.toDoubleOrNull() ?: 0.0
                val buyPrice = buyPriceText.toDoubleOrNull() ?: 0.0
                val totalSpent = amount * buyPrice
                if (totalSpent > 0.0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CryptoDarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Investido:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${selectedCurrency.symbol} ${String.format("%,.2f", totalSpent)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val buyPriceSelected = buyPriceText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Insira uma quantidade válida maior que zero."
                        return@Button
                    }
                    if (buyPriceSelected == null || buyPriceSelected <= 0.0) {
                        errorMessage = "Insira um preço de compra válido."
                        return@Button
                    }
                    // Convert buy price back to USD for normalized persistence
                    val buyPriceUsd = buyPriceSelected / selectedCurrency.rateFromUsd
                    onConfirm(amount, buyPriceUsd)
                },
                modifier = Modifier.testTag("dialog_confirm_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Salvar Ativo",
                    fontWeight = FontWeight.Bold,
                    color = CryptoDarkBackground
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
