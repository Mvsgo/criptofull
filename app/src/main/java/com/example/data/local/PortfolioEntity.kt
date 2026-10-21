package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_items")
data class PortfolioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coinId: String,
    val coinSymbol: String,
    val coinName: String,
    val coinImage: String? = null,
    val amount: Double,
    val buyPriceUsd: Double,
    val timestamp: Long = System.currentTimeMillis()
)
