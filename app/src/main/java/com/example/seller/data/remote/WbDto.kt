package com.example.seller.data.remote

data class WbOrdersResponse(val orders: List<WbOrderDto>?)
data class WbOrderDto(
    val id: Long,
    val orderUid: String,
    val groupId: String,
    val nmId: String,
    val article: String,
    val salePrice: Double,
    val createdAt: String,
    val comment: String?,
    val address: WbAddressDto?,
)
data class WbStatusResponse(
    val orders: List<OrderStatusDto>
)
data class OrderStatusDto(
    val orderId: Long,
    val supplierStatus: String,
    val wbStatus: String
)
data class DeliveryDateResponse(val orders: List<DeliveryDateDto>?)
data class DeliveryDateDto(val id: Long, val dDate: String)

data class GroupInfoDto(val groupID: String, val deliveryCost: Double)
data class WbGroupRequest(
    val groups: List<String>
)
data class WbStatusRequest(
    val ordersIds: List<Long>
)
data class WbAddressDto(
    val fullAddress: String? = null,
    val longitude: Double? = null,
    val latitude: Double? = null
)