package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tax_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val dateFormatted: String, // e.g. "2026-03-15 14:30"
    val exchange: String, // "Binance", "Mercado Bitcoin", "Bybit", "Foxbit", "Carteira Própria", etc.
    val type: String, // "COMPRA", "VENDA", "PERMUTA", "TRANSFERENCIA", "RENDIMENTO"
    val coinSymbol: String, // e.g. "BTC"
    val coinName: String, // e.g. "Bitcoin"
    val amount: Double,
    val unitPriceBrl: Double,
    val feeBrl: Double = 0.0,
    val totalBrl: Double, // Valor total em Reais (compras: custo; vendas: receita bruta)
    val targetCoinSymbol: String? = null, // Usado em Permutas
    val targetAmount: Double? = null,
    val notes: String? = null
)
