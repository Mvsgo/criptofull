package com.example.ui.screens.tax

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.components.CryptoComboBox
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnnualIrpfReport
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
import com.example.util.PdfReportGenerator
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
                    text = "Declaração de Ajuste Anual e Ficha de Bens & Direitos",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                CryptoComboBox(
                    label = "Ano-Calendário da Declaração",
                    selectedOption = "$selectedYear",
                    options = years.map { "$it" },
                    onOptionSelected = { yStr ->
                        yStr.toIntOrNull()?.let { viewModel.setSelectedYear(it) }
                    },
                    leadingIcon = Icons.Default.CalendarMonth,
                    testTag = "irpf_year_combo"
                )
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
                        text = "Resumo Fiscal Anual • $selectedYear",
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
                                text = "Item 05 (Alienação <= 35k)",
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

            // Summary of Assets & Custody on 31/12
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
                        text = "Posição em Custódia em 31/12/$selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ativos Declarados",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${report.assetsPositionDecember31.size} criptoativos",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Custo Total de Aquisição",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            val totalCost = report.assetsPositionDecember31.sumOf { it.totalAcquisitionCostBrl }
                            Text(
                                text = "R$ ${String.format("%,.2f", totalCost)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CryptoGreen
                            )
                        }
                    }
                }
            }

            // Export Complete PDF Report Button
            item {
                Button(
                    onClick = {
                        try {
                            PdfReportGenerator.generateAndSharePdf(context, report)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao gerar PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("export_pdf_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = CryptoDarkBackground,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Exportar PDF do Relatório Completo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CryptoDarkBackground
                    )
                }
            }

            // Explanatory note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CryptoDarkSurfaceVariant.copy(alpha = 0.4f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Conteúdo do Relatório em PDF",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "O documento PDF exportado contém o resumo contábil anual consolidado, apuração mês a mês com separação de vendas isentas e tributáveis, cálculo de imposto devido e todas as fichas individuais de Bens e Direitos com CNPJs e discriminações conforme IN RFB 1888/2019.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
