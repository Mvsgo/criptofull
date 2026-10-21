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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MonthlyTaxReport
import com.example.model.SaleCalculationDetail
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
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CryptoViewModel
import com.example.viewmodel.Screen

@Composable
fun TaxApuracaoScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val monthlyReports by viewModel.monthlyReports.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var selectedReportForDetails by remember { mutableStateOf<MonthlyTaxReport?>(null) }

    val latestMonth = monthlyReports.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CriptoFull",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanNeon.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "IRPF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        text = "Apurador Fiscal de Criptomoedas & DARF",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Highlight: Latest Month Status & DARF
        if (latestMonth != null) {
            item {
                LatestMonthDarfCard(
                    report = latestMonth,
                    onViewDetails = { selectedReportForDetails = latestMonth }
                )
            }
        }

        // Summary Bar: Exemption Limit Banner (R$ 35.000 / mês)
        item {
            ExemptionRuleInfoCard()
        }

        // History of Monthly Tax Reports
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meses Apurados (${monthlyReports.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (transactions.isNotEmpty()) {
                    Text(
                        text = "${transactions.size} operações registradas",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        if (monthlyReports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CryptoDarkSurface)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nenhuma operação registrada ainda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Importe o extrato da sua corretora (Binance, Mercado Bitcoin...) ou adicione compras e vendas para apurar os impostos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Importar) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Importar Extrato de Corretora", color = CryptoDarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(monthlyReports, key = { "${it.year}_${it.month}" }) { report ->
                MonthlyTaxReportItemCard(
                    report = report,
                    onClick = { selectedReportForDetails = report }
                )
            }
        }
    }

    // Modal Details of the Selected Month
    if (selectedReportForDetails != null) {
        MonthDetailsDialog(
            report = selectedReportForDetails!!,
            onDismiss = { selectedReportForDetails = null }
        )
    }
}

@Composable
fun LatestMonthDarfCard(
    report: MonthlyTaxReport,
    onViewDetails: () -> Unit
) {
    val hasDarf = report.taxDueBrl >= 10.0 // Mínimo para DARF no Brasil é R$ 10,00

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (hasDarf) {
                        listOf(Color(0xFF2A151E), CryptoDarkSurface)
                    } else {
                        listOf(Color(0xFF0F2624), CryptoDarkSurface)
                    }
                )
            )
            .border(
                1.dp,
                if (hasDarf) CryptoRed.copy(alpha = 0.5f) else CryptoGreen.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
            .testTag("latest_month_darf_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apuração Mês ${report.monthFormatted}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Isenção Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (report.isExempt) CryptoGreenBg else CryptoRedBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (report.isExempt) "ISENTO (< 35k)" else "TRIBUTÁVEL (> 35k)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (report.isExempt) CryptoGreen else CryptoRed,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DARF Value or Exemption Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasDarf) "DARF A PAGAR (Cód. 4600)" else "IMPOSTO DEVIDO NO MÊS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasDarf) CryptoRed else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasDarf) "R$ ${String.format("%,.2f", report.taxDueBrl)}" else "R$ 0,00",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (hasDarf) CryptoRed else CryptoGreen
                    )
                }

                if (hasDarf) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Vencimento do DARF:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = report.darfDueDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BitcoinGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub metrics: Total Alienado, Lucro Apurado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoDarkSurfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Total Alienado:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "R$ ${String.format("%,.2f", report.totalSalesBrl)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (report.isExempt) "Lucro Isento:" else "Lucro Tributável:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    val profit = if (report.isExempt) report.exemptProfitBrl else report.taxableProfitBrl
                    Text(
                        text = "R$ ${String.format("%,.2f", profit)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (profit >= 0) CryptoGreen else CryptoRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CryptoDarkSurfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ver Detalhamento das Vendas do Mês", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ExemptionRuleInfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CryptoDarkSurface)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = CyanNeon,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "Regra de Isenção da Receita Federal",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Ganhos em meses com vendas totais de criptoativos de até R$ 35.000,00 são isentos de IRPF. Vendas acima de R$ 35k tributam 15% sobre o ganho de capital.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun MonthlyTaxReportItemCard(
    report: MonthlyTaxReport,
    onClick: () -> Unit
) {
    val hasDarf = report.taxDueBrl >= 10.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CryptoDarkSurface)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("month_report_${report.monthFormatted}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.monthFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (report.isExempt) CryptoGreenBg else CryptoRedBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (report.isExempt) "Isento" else "Tributável",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (report.isExempt) CryptoGreen else CryptoRed,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (hasDarf) "DARF: R$ ${String.format("%,.2f", report.taxDueBrl)}" else "DARF: R$ 0,00",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (hasDarf) CryptoRed else CryptoGreen
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.1f)) {
                Text(text = "Total Alienado:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 11.sp)
                Text(
                    text = "R$ ${String.format("%,.2f", report.totalSalesBrl)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Resultado:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 11.sp)
                val netResult = report.grossProfitBrl - report.grossLossBrl
                Text(
                    text = "R$ ${String.format("%,.2f", netResult)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (netResult >= 0) CryptoGreen else CryptoRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Vendas:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 11.sp)
                Text(
                    text = "${report.sales.size} op.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanNeon,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MonthDetailsDialog(
    report: MonthlyTaxReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoDarkSurface,
        title = {
            Column {
                Text(
                    text = "Detalhamento - Mês ${report.monthFormatted}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (report.isExempt) "Total de Vendas até R$ 35.000 (Isento de Imposto)" else "Total de Vendas superior a R$ 35.000 (Tributável)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (report.isExempt) CryptoGreen else CryptoRed
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
                // Key metrics box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CryptoDarkSurfaceVariant)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DetailRow("Total de Vendas no Mês:", "R$ ${String.format("%,.2f", report.totalSalesBrl)}")
                    DetailRow("Lucro Isento Apurado:", "R$ ${String.format("%,.2f", report.exemptProfitBrl)}")
                    DetailRow("Lucro Tributável:", "R$ ${String.format("%,.2f", report.taxableProfitBrl)}")
                    if (report.lossCompensatedBrl > 0) {
                        DetailRow("Prejuízo Compensado:", "R$ ${String.format("%,.2f", report.lossCompensatedBrl)}")
                    }
                    DetailRow("Base de Cálculo Líquida:", "R$ ${String.format("%,.2f", report.netTaxableBaseBrl)}")
                    DetailRow("Imposto Devido (15%):", "R$ ${String.format("%,.2f", report.taxDueBrl)}", isBold = true, color = if (report.taxDueBrl > 0) CryptoRed else CryptoGreen)
                    if (report.darfDueDate.isNotBlank() && report.taxDueBrl >= 10.0) {
                        DetailRow("Vencimento do DARF:", report.darfDueDate, color = BitcoinGold)
                    }
                }

                Text(
                    text = "Vendas Executadas (${report.sales.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (report.sales.isEmpty()) {
                    Text(text = "Nenhuma venda registrada neste mês.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                } else {
                    report.sales.forEach { sale ->
                        SaleDetailCard(sale)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Fechar", color = CryptoDarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
fun SaleDetailCard(sale: SaleCalculationDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CryptoDarkSurfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${sale.dateString} • ${sale.coinSymbol}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${if (sale.isProfit) "+" else ""}R$ ${String.format("%,.2f", sale.profitOrLossBrl)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (sale.isProfit) CryptoGreen else CryptoRed
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Qtd: ${sale.amountSold} • Custo: R$ ${String.format("%,.2f", sale.averageCostPriceBrl)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
            Text(
                text = "Venda: R$ ${String.format("%,.2f", sale.saleGrossValueBrl)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
