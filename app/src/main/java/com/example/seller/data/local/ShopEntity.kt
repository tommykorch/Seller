package com.example.seller.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val token: String,
    val type: String // "dbs" или "dbw"
)