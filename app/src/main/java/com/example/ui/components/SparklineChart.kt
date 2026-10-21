package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.ChartGreenEnd
import com.example.ui.theme.ChartGreenStart
import com.example.ui.theme.ChartRedEnd
import com.example.ui.theme.ChartRedStart
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed

@Composable
fun SparklineChart(
    prices: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    if (prices.size < 2) return

    val strokeColor = if (isPositive) CryptoGreen else CryptoRed
    val gradientColors = if (isPositive) {
        listOf(ChartGreenStart.copy(alpha = 0.35f), ChartGreenEnd)
    } else {
        listOf(ChartRedStart.copy(alpha = 0.35f), ChartRedEnd)
    }

    val minPrice = remember(prices) { prices.minOrNull() ?: 0.0 }
    val maxPrice = remember(prices) { prices.maxOrNull() ?: 1.0 }
    val range = if (maxPrice - minPrice > 0) maxPrice - minPrice else 1.0

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (prices.size - 1)

        val strokePath = Path()
        val fillPath = Path()

        prices.forEachIndexed { index, price ->
            val normY = (1f - ((price - minPrice) / range).toFloat()).coerceIn(0f, 1f)
            val x = index * stepX
            val y = normY * height * 0.85f + (height * 0.075f)

            if (index == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == prices.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Fill gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = 0f,
                endY = height
            )
        )

        // Line
        drawPath(
            path = strokePath,
            color = strokeColor,
            style = Stroke(width = 2f)
        )
    }
}
