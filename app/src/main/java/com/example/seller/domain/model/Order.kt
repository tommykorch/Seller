package com.example.seller.domain.model

data class Order(
    val id: Long,
    val shopName: String,
    val nmId: String,
    val article: String,
    val salePrice: Double,
    val comment: String?,
    val createdAt: String,
    val deliveryDate: String?,
    val status: OrderStatus = OrderStatus.NEW
)