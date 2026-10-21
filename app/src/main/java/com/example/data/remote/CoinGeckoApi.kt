package com.example.data.remote

import com.example.model.Coin
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("api/v3/coins/markets")
    suspend fun getCoinsMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChangePercentage: String = "24h"
    ): List<Coin>
}
