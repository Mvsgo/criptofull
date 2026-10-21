package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_coins")
data class FavoriteEntity(
    @PrimaryKey
    val coinId: String,
    val addedAt: Long = System.currentTimeMillis()
)
