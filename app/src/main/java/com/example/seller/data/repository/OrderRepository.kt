package com.example.seller.data.repository

import android.util.Log
import com.example.seller.data.local.*
import com.example.seller.data.remote.*
import kotlinx.coroutines.flow.Flow

class OrderRepository(
    private val api: WbApi,
    private val dao: AppDao
) {
    private val TAG = "WB_REPO_LOG"

    // --- РАБОТА С МАГАЗИНАМИ ---
    fun getShops(): Flow<List<ShopEntity>> = dao.getAllShops()

    suspend fun saveShop(shop: ShopEntity) = dao.insertShop(shop)

    suspend fun deleteShop(id: Int) = dao.deleteShop(id)

    // --- РАБОТА С ЗАКАЗАМИ ---
    fun getOrders(status: String): Flow<List<OrderEntity>> = dao.getOrdersByStatus(status)

    suspend fun updateStatus(orderId: Long, status: String) = dao.updateOrderStatus(orderId, status)

    suspend fun refreshOrdersForShop(shop: ShopEntity) {
        try {
            Log.d(TAG, "Запрос новых заказов для магазина: ${shop.name}")

            val response = api.getNewOrders(shop.type, shop.token)

            if (!response.isSuccessful) {
                Log.e(TAG, "Ошибка API новых заказов! Код: ${response.code()} Тело: ${response.errorBody()?.string()}")
                return
            }

            val wbOrders = response.body()?.orders ?: run {
                Log.w(TAG, "Список заказов пуст или null")
                return
            }
            Log.d(TAG, "Найдено заказов: ${wbOrders.size}")

            val orderIds = wbOrders.map { it.id }
            val deliveryRequest = WbDeliveryRequest(orders = orderIds)

            Log.d(TAG, "Запрос дат доставки для ID: $orderIds")
            val dateResponse = api.getDeliveryDates(shop.token, deliveryRequest)

            val dateMap = if (dateResponse.isSuccessful) {
                dateResponse.body()?.orders?.associate { it.id to it.dDate } ?: emptyMap()
            } else {
                Log.e(TAG, "Ошибка получения дат! Код: ${dateResponse.code()}")
                emptyMap()
            }

            /*val groupIds = wbOrders.map { it.groupId }.distinct() // Берем уникальные ID групп
            val groupResponse = api.getGroupInfo(shop.token, mapOf("groups" to groupIds))
            Log.d(TAG, "Запрос ЦЕНЫ доставки для groupIds: $groupIds")
            val deliveryCostMap = if (groupResponse.isSuccessful) {
                groupResponse.body()?.associate { it.groupID to it.deliveryCost } ?: emptyMap()
            } else {
                Log.e(TAG, "Ошибка получения ЦЕНЫ ДОСТАВКИ! Код: ${dateResponse.code()}")
                emptyMap()
            }*/

            val entities = wbOrders.map { dto ->
                OrderEntity(
                    id = dto.id,
                    shopName = shop.name,
                    nmId = dto.nmId,
                    article = dto.article,
                    salePrice = dto.salePrice / 100.0,
                    //deliveryCost = (deliveryCostMap[dto.groupId] ?: 0.0) / 100.0,
                    comment = dto.comment,
                    createdAt = dto.createdAt,
                    deliveryDate = dateMap[dto.id],
                    status = "NEW"
                )
            }

            dao.insertOrders(entities)
            Log.d(TAG, "Данные успешно отправлены в Room DB: ${entities.size} шт.")

        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка в refreshOrdersForShop: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
}