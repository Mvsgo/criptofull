package com.example.ui.screens.tax

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnnualIrpfReport
import com.example.model.IrpfAssetDeclaration
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenBg
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CryptoViewModel
import java.util.Calendar

@Composable
fun TaxRelatoriosScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val annualReport by viewModel.annualReport.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = listOf(currentYear, currentYear - 1, currentYear - 2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Year Selector
        item {
            Column {
                Text(
                    text = "Relatórios IRPF",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Declaração de Ajuste Anual da Receita Federal (Bens e Direitos)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Selecione o Ano-Calendário:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    years.forEach { y ->
                        val isSelected = selectedYear == y
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = isSelected,
                            onClick = { viewModel.setSelectedYear(y) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isSelected) CyanNeon else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = "$y",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
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
        }

        if (annualReport != null) {
            val report = annualReport!!

            // Annual Income Summary Card (Isentos vs Tributáveis)
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
                        text = "Resumo Fiscal do Ano-Calendário $selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Isentos
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Rendimentos Isentos",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Item 05 (Vendas <= 35k)",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanNeon,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "R$ ${String.format("%,.2f", report.totalExemptProfitsYearBrl)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CryptoGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Tributáveis
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CryptoDarkSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Ganhos Tributáveis",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Tributação Exclusiva",
                                style = MaterialTheme.typography.labelSmall,
                                color = BitcoinGold,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "R$ ${String.format("%,.2f", report.totalTaxableProfitsYearBrl)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CryptoDarkSurfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total de Imposto Devido no Ano:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(end = 6.dp)
                        )
                        Text(
                            text = "R$ ${String.format("%,.2f", report.totalTaxesDueYearBrl)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (report.totalTaxesDueYearBrl > 0) BitcoinGold else CryptoGreen
                        )
                    }
                }
            }

            // Share / Export Full Report Button
            item {
                Button(
                    onClick = {
                        val fullReportText = generateFullReportText(report)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, "Relatório IRPF CriptoFull $selectedYear")
                            putExtra(Intent.EXTRA_TEXT, fullReportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Exportar Relatório IRPF"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = CryptoDarkBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exportar / Compartilhar Relatório Completo",
                        fontWeight = FontWeight.Bold,
                        color = CryptoDarkBackground
                    )
                }
            }

            // Bens e Direitos Header
            item {
                Column {
                    Text(
                        text = "Ficha de Bens e Direitos (Posição em 31/12/$selectedYear)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Copie a discriminação de cada ativo e cole diretamente no programa IRPF da Receita Federal",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Asset Declaration Cards
            if (report.assetsPositionDecember31.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum criptoativo em estoque no final do ano-calendário $selectedYear.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                items(report.assetsPositionDecember31, key = { it.coinSymbol }) { asset ->
                    IrpfAssetDeclarationCard(
                        asset = asset,
                        year = selectedYear,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Discriminação IRPF", asset.formattedDiscrimination))
                            Toast.makeText(context, "Discriminação de ${asset.coinSymbol} copiada!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun IrpfAssetDeclarationCard(
    asset: IrpfAssetDeclaration,
    year: Int,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CryptoDarkSurface)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("irpf_asset_${asset.coinSymbol}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${asset.coinName} (${asset.coinSymbol})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Grupo ${asset.irpfCategory.group} • Código ${asset.irpfCategory.code} - ${asset.irpfCategory.description}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanNeon,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CryptoDarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = asset.exchangeName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Financial values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Qtd. em 31/12:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    text = "${asset.quantity} ${asset.coinSymbol}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Situação em 31/12/$year:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    text = "R$ ${String.format("%,.2f", asset.totalAcquisitionCostBrl)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = CryptoGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Discrimination Text Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CryptoDarkSurfaceVariant)
                .padding(10.dp)
        ) {
            Text(
                text = "Discriminação para o IRPF:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = CyanNeon
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = asset.formattedDiscrimination,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onCopy,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyanNeon))
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copiar Discriminação para o Programa IRPF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun generateFullReportText(report: AnnualIrpfReport): String {
    val sb = StringBuilder()
    sb.append("=== CRIPTOFULL - RELATÓRIO ANUAL IRPF ${report.year} ===\n\n")
    sb.append("1. RENDIMENTOS ISENTOS E NÃO TRIBUTÁVEIS:\n")
    sb.append("   Item 05 - Ganho de capital na alienação de criptoativos (alienações <= R$ 35.000/mês)\n")
    sb.append("   Total Isento no Ano: R$ ${String.format("%,.2f", report.totalExemptProfitsYearBrl)}\n\n")

    sb.append("2. RENDIMENTOS SUJEITOS À TRIBUTAÇÃO EXCLUSIVA / DEFINITIVA:\n")
    sb.append("   Ganhos de Capital em meses tributáveis: R$ ${String.format("%,.2f", report.totalTaxableProfitsYearBrl)}\n")
    sb.append("   Total de Imposto Apurado no Ano: R$ ${String.format("%,.2f", report.totalTaxesDueYearBrl)}\n\n")

    sb.append("3. FICHA BENS E DIREITOS (SITUAÇÃO EM 31/12/${report.year}):\n")
    sb.append("------------------------------------------------------------\n")
    report.assetsPositionDecember31.forEachIndexed { idx, asset ->
        sb.append("[Item ${idx + 1}]\n")
        sb.append("Grupo: ${asset.irpfCategory.group} - Criptoativos\n")
        sb.append("Código: ${asset.irpfCategory.code} - ${asset.irpfCategory.description}\n")
        sb.append("Localização: 105 - Brasil (ou país da corretora)\n")
        sb.append("Situação em 31/12/${report.year}: R$ ${String.format("%,.2f", asset.totalAcquisitionCostBrl)}\n")
        sb.append("Discriminação:\n${asset.formattedDiscrimination}\n\n")
    }

    sb.append("------------------------------------------------------------\n")
    sb.append("Gerado automaticamente pelo aplicativo CriptoFull.\n")
    return sb.toString()
}
