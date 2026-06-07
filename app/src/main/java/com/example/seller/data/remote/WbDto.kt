package com.example.seller.data.remote

data class WbOrdersResponse(val orders: List<WbOrderDto>?)
data class WbOrderDto(
    val id: Long,
    val nmId: String,
    val article: String,
    val salePrice: Double,
    val groupId: String,
    val comment: String?,
    val createdAt: String
)

data class DeliveryDateResponse(val orders: List<DeliveryDateDto>?)
data class DeliveryDateDto(val id: Long, val dDate: String)

data class GroupInfoDto(val groupID: String, val deliveryCost: Double)