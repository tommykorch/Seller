package com.example.seller.domain.model

data class Shop(
    val id: Int = 0,
    val name: String,
    val token: String,
    val type: String // "dbs" или "dbw"
)