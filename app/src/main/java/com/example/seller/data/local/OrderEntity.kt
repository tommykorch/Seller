package com.example.seller.data.local
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        )
])
data class OrderEntity(
    @PrimaryKey val id: Long,
    val shopId: Int,
    val shopName: String,
    val nmId: String,
    val article: String,
    val salePrice: Double,
    val deliveryCost: Double = 0.0,
    val comment: String?,
    val createdAt: String,
    val deliveryDate: String?,
    val status: String,
    val supplierStatus: String? = null,
    val wbStatus: String? = null,
    val fullAddress: String?
)