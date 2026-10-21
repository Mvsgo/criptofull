package com.example.model

data class MarketStats(
    val totalMarketCapUsd: Double = 2_680_000_000_000.0,
    val total24hVolumeUsd: Double = 84_500_000_000.0,
    val btcDominancePercentage: Double = 56.4,
    val ethDominancePercentage: Double = 14.8,
    val marketCapChangePercentage24h: Double = 2.45,
    val fearAndGreedIndex: Int = 68,
    val fearAndGreedLabel: String = "Ganância" // "Extremo Medo", "Medo", "Neutro", "Ganância", "Extrema Ganância"
)
