package com.example.seller.data.remote

import retrofit2.Response
import retrofit2.http.*

interface WbApi {
    @GET("api/v3/{type}/orders/new")
    suspend fun getNewOrders(
        @Path("type") type: String,
        @Header("Authorization") token: String
    ): Response<WbOrdersResponse>

    @POST("api/v3/dbs/orders/delivery-date")
    suspend fun getDeliveryDates(
        @Header("Authorization") token: String,
        @Body body: WbDeliveryRequest
    ): Response<DeliveryDatesResponse>

    @POST("api/v3/dbs/groups/info")
    suspend fun getGroupInfo(
        @Header("Authorization") token: String,
        @Body body: WbGroupRequest
    ): Response<List<GroupInfoDto>>
    @POST("api/v3/dbs/orders/status") // проверьте правильность пути
    suspend fun getOrdersStatusInfo(
        @Header("Authorization") token: String,
        @Body body: WbStatusRequest // Используем конкретный класс вместо Map
    ): Response<WbStatusResponse>

    @GET("api/v3/dbs/orders")
    suspend fun getOrdersByPeriod(
        @Query("type") type: String,
        @Header("Authorization") token: String,
        @Query("dateFrom") dateFrom: Long,
        @Query("dateTo") dateTo: Long,
        @Query("limit") limit: Int = 1000,
        @Query("next") next: Int = 0
    ): Response<WbOrdersResponse>

}