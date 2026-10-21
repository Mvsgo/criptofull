package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.viewmodel.CryptoViewModel

@Composable
fun ConverterScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val coins by viewModel.coins.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    var sourceCoin by remember(coins) {
        mutableStateOf(coins.firstOrNull { it.symbol.lowercase() == "btc" } ?: coins.firstOrNull())
    }
    var targetCurrency by remember { mutableStateOf(Currency.BRL) }
    var inputAmountText by remember { mutableStateOf("1") }
    var coinMenuExpanded by remember { mutableStateOf(false) }
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    val amount = inputAmountText.toDoubleOrNull() ?: 0.0
    val sourcePriceUsd = sourceCoin?.currentPrice ?: 0.0
    val totalValueInUsd = amount * sourcePriceUsd
    val convertedResult = totalValueInUsd * targetCurrency.rateFromUsd

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        item {
            Column {
                Text(
                    text = "Conversor Cripto",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Calculadora instantânea de cotações e paridades em tempo real",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // Converter Card Container
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CryptoDarkSurface)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                // Section "De" (From Crypto)
                Text(
                    text = "Você envia / Converte",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Crypto Selector
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .clickable { coinMenuExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("select_source_coin_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (sourceCoin != null && !sourceCoin!!.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = sourceCoin!!.image,
                                    contentDescription = sourceCoin!!.name,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = sourceCoin?.symbol?.uppercase() ?: "BTC",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = coinMenuExpanded,
                            onDismissRequest = { coinMenuExpanded = false },
                            modifier = Modifier.background(CryptoDarkSurfaceVariant)
                        ) {
                            coins.forEach { coin ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${coin.name} (${coin.symbol.uppercase()})",
                                            color = TextPrimary
                                        )
                                    },
                                    onClick = {
                                        sourceCoin = coin
                                        coinMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Amount Text Field
                    OutlinedTextField(
                        value = inputAmountText,
                        onValueChange = { inputAmountText = it.replace(',', '.') },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("converter_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CryptoDarkSurfaceVariant,
                            unfocusedContainerColor = CryptoDarkSurfaceVariant,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Quick Amount Preset Chips
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("0.1", "0.5", "1", "5", "10").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .clickable { inputAmountText = preset }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanNeon,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Swap Icon Divider
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanNeon)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Inverter",
                            tint = CryptoDarkBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Section "Para" (To Fiat Currency)
                Text(
                    text = "Você recebe / Equivalente",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Currency Selector
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .clickable { currencyMenuExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("select_target_currency_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${targetCurrency.symbol} ${targetCurrency.code}",
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = currencyMenuExpanded,
                            onDismissRequest = { currencyMenuExpanded = false },
                            modifier = Modifier.background(CryptoDarkSurfaceVariant)
                        ) {
                            Currency.values().forEach { cur ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${cur.symbol} ${cur.displayName} (${cur.code})",
                                            color = TextPrimary
                                        )
                                    },
                                    onClick = {
                                        targetCurrency = cur
                                        currencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Result Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CryptoDarkSurfaceVariant)
                            .border(1.dp, CryptoCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "${targetCurrency.symbol} ${String.format("%,.2f", convertedResult)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                }
            }
        }

        // Live Rate Reference Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CryptoDarkSurface)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Taxa de Câmbio de Referência",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1 ${sourceCoin?.symbol?.uppercase() ?: "BTC"}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${targetCurrency.symbol} ${String.format("%,.2f", sourcePriceUsd * targetCurrency.rateFromUsd)}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1 ${targetCurrency.code}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    val inverse = if (sourcePriceUsd > 0) 1.0 / (sourcePriceUsd * targetCurrency.rateFromUsd) else 0.0
                    Text(
                        text = "${String.format("%.8f", inverse)} ${sourceCoin?.symbol?.uppercase() ?: "BTC"}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
