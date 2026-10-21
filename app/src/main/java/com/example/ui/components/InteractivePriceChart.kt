package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Currency
import com.example.ui.theme.ChartCyanEnd
import com.example.ui.theme.ChartCyanStart
import com.example.ui.theme.ChartGreenEnd
import com.example.ui.theme.ChartGreenStart
import com.example.ui.theme.ChartRedEnd
import com.example.ui.theme.ChartRedStart
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

@Composable
fun InteractivePriceChart(
    basePrices: List<Double>,
    currentPriceUsd: Double,
    currency: Currency,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf("7D") }
    val timeframes = listOf("24H", "7D", "1M", "1A")

    // Derive points based on timeframe
    val chartPrices = remember(basePrices, selectedTimeframe, currentPriceUsd) {
        val base = if (basePrices.isNotEmpty()) basePrices else listOf(currentPriceUsd * 0.95, currentPriceUsd)
        when (selectedTimeframe) {
            "24H" -> {
                // Generate 24 points around current price
                val start = base.lastOrNull() ?: currentPriceUsd
                List(24) { i ->
                    val factor = 1.0 + (i - 12) * 0.003 + (sinVariation(i) * 0.008)
                    start * factor
                }
            }
            "7D" -> {
                if (base.size >= 7) base else List(7) { i -> currentPriceUsd * (0.92 + i * 0.015) }
            }
            "1M" -> {
                List(30) { i ->
                    val trend = if (isPositive) 0.88 + (i * 0.004) else 1.12 - (i * 0.004)
                    currentPriceUsd * (trend + sinVariation(i) * 0.03)
                }
            }
            "1A" -> {
                List(52) { i ->
                    val trend = 0.55 + (i * 0.009)
                    currentPriceUsd * (trend + sinVariation(i) * 0.05)
                }
            }
            else -> base
        }
    }

    var touchX by remember { mutableFloatStateOf(-1f) }
    var touchedPrice by remember { mutableStateOf<Double?>(null) }

    val strokeColor = if (isPositive) CryptoGreen else CryptoRed
    val gradientColors = if (isPositive) {
        listOf(ChartGreenStart.copy(alpha = 0.4f), ChartGreenEnd)
    } else {
        listOf(ChartRedStart.copy(alpha = 0.4f), ChartRedEnd)
    }

    val minPrice = remember(chartPrices) { chartPrices.minOrNull() ?: 0.0 }
    val maxPrice = remember(chartPrices) { chartPrices.maxOrNull() ?: 1.0 }
    val range = if (maxPrice - minPrice > 0) maxPrice - minPrice else 1.0

    Column(modifier = modifier) {
        // Active price display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val displayPrice = touchedPrice ?: currentPriceUsd
                Text(
                    text = currency.format(displayPrice),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (touchedPrice != null) "Cotação no ponto selecionado" else "Cotação atual ao vivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (touchedPrice != null) CyanNeon else TextSecondary
                )
            }

            // Min & Max range
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Máx: ${currency.format(maxPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CryptoGreen
                )
                Text(
                    text = "Mín: ${currency.format(minPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CryptoRed
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(CryptoDarkSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(8.dp)
                .pointerInput(chartPrices) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchX = offset.x
                        },
                        onDragEnd = {
                            touchX = -1f
                            touchedPrice = null
                        },
                        onDragCancel = {
                            touchX = -1f
                            touchedPrice = null
                        },
                        onDrag = { change, _ ->
                            touchX = change.position.x
                        }
                    )
                }
                .pointerInput(chartPrices) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchX = offset.x
                            tryAwaitRelease()
                            touchX = -1f
                            touchedPrice = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val count = chartPrices.size
                if (count < 2) return@Canvas

                val stepX = width / (count - 1)
                val strokePath = Path()
                val fillPath = Path()

                // Calculate touched point index
                if (touchX >= 0f) {
                    val clampedX = touchX.coerceIn(0f, width)
                    val index = ((clampedX / width) * (count - 1)).toInt().coerceIn(0, count - 1)
                    touchedPrice = chartPrices[index]
                }

                chartPrices.forEachIndexed { index, price ->
                    val normY = (1f - ((price - minPrice) / range).toFloat()).coerceIn(0f, 1f)
                    val x = index * stepX
                    val y = normY * (height * 0.8f) + (height * 0.1f)

                    if (index == 0) {
                        strokePath.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        strokePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }

                    if (index == count - 1) {
                        fillPath.lineTo(x, height)
                        fillPath.close()
                    }
                }

                // Fill gradient area
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = height
                    )
                )

                // Stroke line
                drawPath(
                    path = strokePath,
                    color = strokeColor,
                    style = Stroke(width = 3.5f)
                )

                // Touch indicator scrubber line and dot
                if (touchX >= 0f) {
                    val clampedX = touchX.coerceIn(0f, width)
                    val index = ((clampedX / width) * (count - 1)).toInt().coerceIn(0, count - 1)
                    val price = chartPrices[index]
                    val normY = (1f - ((price - minPrice) / range).toFloat()).coerceIn(0f, 1f)
                    val exactX = index * stepX
                    val exactY = normY * (height * 0.8f) + (height * 0.1f)

                    // Vertical dashed line
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(exactX, 0f),
                        end = Offset(exactX, height),
                        strokeWidth = 2f
                    )

                    // Outer glowing circle
                    drawCircle(
                        color = CyanNeon.copy(alpha = 0.4f),
                        radius = 12f,
                        center = Offset(exactX, exactY)
                    )

                    // Inner solid circle
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(exactX, exactY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timeframe selector chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            timeframes.forEach { tf ->
                val isSelected = selectedTimeframe == tf
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTimeframe = tf },
                    label = {
                        Text(
                            text = tf,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                        selectedLabelColor = CyanNeon,
                        containerColor = CryptoDarkSurfaceVariant.copy(alpha = 0.6f),
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) CyanNeon else Color.Transparent
                    )
                )
            }
        }
    }
}

private fun sinVariation(step: Int): Double {
    return kotlin.math.sin(step * 0.45)
}
