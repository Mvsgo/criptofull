package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SparklineData(
    @Json(name = "price") val price: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class Coin(
    @Json(name = "id") val id: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String? = null,
    @Json(name = "current_price") val currentPrice: Double = 0.0,
    @Json(name = "market_cap") val marketCap: Double = 0.0,
    @Json(name = "market_cap_rank") val marketCapRank: Int = 0,
    @Json(name = "total_volume") val totalVolume: Double = 0.0,
    @Json(name = "high_24h") val high24h: Double = 0.0,
    @Json(name = "low_24h") val low24h: Double = 0.0,
    @Json(name = "price_change_24h") val priceChange24h: Double = 0.0,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double = 0.0,
    @Json(name = "circulating_supply") val circulatingSupply: Double = 0.0,
    @Json(name = "ath") val allTimeHigh: Double = 0.0,
    @Json(name = "sparkline_in_7d") val sparklineIn7d: SparklineData? = null,
    val category: String = "Layer 1",
    val description: String = ""
) {
    val prices7d: List<Double>
        get() = sparklineIn7d?.price?.filter { it > 0.0 } ?: emptyList()

    val isPositive: Boolean
        get() = priceChangePercentage24h >= 0.0
}
