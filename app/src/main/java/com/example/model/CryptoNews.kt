package com.example.model

data class CryptoNews(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val timeAgo: String,
    val category: String, // "Mercado", "DeFi", "Regulação", "Bitcoin", "Airdrop"
    val sentiment: String = "Positivo", // "Positivo", "Neutro", "Alerta"
    val readMinutes: Int = 3
)
