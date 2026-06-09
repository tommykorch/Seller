package com.example.seller.data.repository

import android.util.Log
import com.example.seller.data.local.*
import com.example.seller.data.remote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
class OrderRepository(
    private val api: WbApi,
    private val dao: AppDao
) {

    private val TAG = "WB_REPO_LOG"

    private fun formatIsoDate(isoString: String?): String {
        if (isoString == null || isoString.isEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            val date = inputFormat.parse(isoString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                isoString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга даты $isoString: ${e.message}")
            isoString
        }
    }
    // --- РАБОТА С МАГАЗИНАМИ ---
    fun getShops(): Flow<List<ShopEntity>> = dao.getAllShops()

    suspend fun saveShop(shop: ShopEntity) = dao.insertShop(shop)

    suspend fun deleteShop(id: Int) = dao.deleteShop(id)

    // --- РАБОТА С ЗАКАЗАМИ ---
    fun getOrders(status: String): Flow<List<OrderEntity>> = dao.getOrdersByStatus(status)

    suspend fun updateStatus(orderId: Long, status: String) = dao.updateOrderStatus(orderId, status)


    suspend fun syncAllShopsArchive(dateFrom: Long, dateTo: Long) {
        try {
            // 1. Получаем список всех зарегистрированных магазинов
            val shops = dao.getAllShops().first()

            shops.forEach { shop ->
                try {
                    Log.d(TAG, "Архив: Загрузка данных для магазина ${shop.name}")

                    Log.d(TAG, "Параметры запроса: type=${shop.type}, from=$dateFrom, to=$dateTo")
                    val response = api.getOrdersByPeriod(shop.type, shop.token, dateFrom, dateTo,limit = 1000,next = 0)
                    Log.d(TAG, "Ответ от API: ${response.code()} ${response.message()}")
                    val wbOrders = response.body()?.orders ?: return@forEach

                    if (wbOrders.isEmpty()) {
                        Log.d(TAG, "Архив: Заказов за период не найдено для ${shop.name}")
                        return@forEach
                    }

                    // 3. Получаем статусы для этих заказов (чтобы видеть "Отменено", "Доставлено" и т.д.)
                    val ids = wbOrders.map { it.id }
                    val statusRequest = WbStatusRequest(ordersIds = ids)
                    val statusResponse = api.getOrdersStatusInfo(shop.token, statusRequest)
                    val statusMap = statusResponse.body()?.orders?.associate { it.orderId to it } ?: emptyMap()

                    val groupIds = wbOrders.map { it.groupId }.distinct()

                    val groupRequest = WbGroupRequest(groups = groupIds)
                    //val groupResponse = api.getGroupInfo(shop.token, groupRequest)
                    //val costMap = groupResponse.body()?.associate { it.groupID to it.deliveryCost } ?: emptyMap()

                    // 5. Маппинг в OrderEntity с пометкой status = "ARCHIVE"
                    val entities = wbOrders.map { dto ->
                        OrderEntity(
                            id = dto.id,
                            shopId=shop.id,
                            shopName = shop.name,
                            nmId = dto.nmId,
                            article = dto.article,
                            salePrice = dto.salePrice / 100.0,
                           //deliveryCost = (costMap[dto.groupId] ?: 0.0) / 100.0,
                            comment = dto.comment,
                            createdAt = formatIsoDate(dto.createdAt),
                            deliveryDate = null,
                            status = "ARCHIVE",
                            supplierStatus = statusMap[dto.id]?.supplierStatus,
                            wbStatus = statusMap[dto.id]?.wbStatus,
                            fullAddress = dto.address?.fullAddress,
                        )
                    }
                    Log.e(TAG, "Все заказы ${entities}")
                    dao.insertOrders(entities)
                    Log.d(TAG, "Архив: Успешно сохранено ${entities.size} заказов для ${shop.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка синхронизации архива магазина ${shop.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка при загрузке архива: ${e.localizedMessage}")
        }
    }

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
            if (wbOrders.isEmpty()) {
                Log.d(TAG, "Новых заказов нет, пропускаем запросы дат и цен.")
                return
            }
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

            // Включаем расчет стоимости доставки и для новых заказов
            val groupIds = wbOrders.map { it.groupId }.distinct()
            //val groupResponse = api.getGroupInfo(shop.token, mapOf("groups" to groupIds))
            val groupRequest = WbGroupRequest(groups = groupIds)
            val groupResponse = api.getGroupInfo(shop.token, groupRequest)
            val deliveryCostMap = if (groupResponse.isSuccessful) {
                groupResponse.body()?.associate { it.groupID to it.deliveryCost } ?: emptyMap()
            } else {
                emptyMap()
            }

            val entities = wbOrders.map { dto ->
                OrderEntity(
                    id = dto.id,
                    shopId=shop.id,
                    shopName = shop.name,
                    nmId = dto.nmId,
                    article = dto.article,
                    salePrice = dto.salePrice / 100.0,
                    deliveryCost = (deliveryCostMap[dto.groupId] ?: 0.0) / 100.0,
                    comment = dto.comment,
                    createdAt = formatIsoDate(dto.createdAt),
                    deliveryDate = formatIsoDate(dateMap[dto.id]),
                    status = "NEW",
                    fullAddress = dto.address?.fullAddress,
                )
            }

            dao.insertOrders(entities)
            Log.d(TAG, "Данные успешно отправлены в Room DB: ${entities.size} шт.")

        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка в refreshOrdersForShop: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
    suspend fun deleteShopAndItsOrders(shopId: Int, shopName: String) {
        dao.deleteOrdersByShopName(shopName)
        dao.deleteShop(shopId)
    }

}