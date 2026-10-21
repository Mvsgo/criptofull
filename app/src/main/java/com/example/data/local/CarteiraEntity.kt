package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carteiras")
data class CarteiraEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String, // ex: "Binance Spot", "Mercado Bitcoin", "Metamask DeFi", "Ledger Nano X"
    val tipo: String = "EXCHANGE", // "EXCHANGE", "HOT_WALLET", "COLD_WALLET", "OUTRA"
    val corretora: String = "Binance", // "Binance", "Mercado Bitcoin", "Bybit", "Foxbit", "Coinbase", "Metamask", "Ledger", "Trezor", "Outra"
    val endereco: String? = null, // Endereço público / hash / rede
    val cor: String = "#00E5FF", // Cor de identificação para UI (#00E5FF, #F59E0B, #10B981, #EC4899, etc.)
    val observacoes: String? = null,
    val criadoEm: Long = System.currentTimeMillis(),
    val ativo: Boolean = true
)
