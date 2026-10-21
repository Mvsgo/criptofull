package com.example.model

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val rateFromUsd: Double // 1 USD = rateFromUsd in this currency
) {
    USD("USD", "$", "Dólar Americano", 1.0),
    BRL("BRL", "R$", "Real Brasileiro", 5.65),
    EUR("EUR", "€", "Euro", 0.92);

    fun format(amountUsd: Double): String {
        val converted = amountUsd * rateFromUsd
        return when {
            converted >= 1_000_000_000 -> "$symbol ${String.format("%.2f", converted / 1_000_000_000)}B"
            converted >= 1_000_000 -> "$symbol ${String.format("%.2f", converted / 1_000_000)}M"
            converted >= 1_000 -> "$symbol ${String.format("%,.2f", converted)}"
            converted >= 1 -> "$symbol ${String.format("%.2f", converted)}"
            converted >= 0.0001 -> "$symbol ${String.format("%.4f", converted)}"
            else -> "$symbol ${String.format("%.6f", converted)}"
        }
    }

    fun formatRaw(value: Double): String {
        return when {
            value >= 1_000 -> "$symbol ${String.format("%,.2f", value)}"
            value >= 1 -> "$symbol ${String.format("%.2f", value)}"
            else -> "$symbol ${String.format("%.4f", value)}"
        }
    }
}
