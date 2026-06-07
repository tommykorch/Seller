package com.example.seller.data.remote

import retrofit2.Response
import retrofit2.http.*

interface WbApi {
    @GET("api/v3/{type}/orders/new")
    suspend fun getNewOrders(
        @Path("type") type: String,
        @Header("Authorization") token: String
    ): Response<WbOrdersResponse>

    @POST("api/v3/{type}/orders/delivery-date")
    suspend fun getDeliveryDates(
        @Path("type") type: String,
        @Header("Authorization") token: String,
        @Body body: Map<String, List<Long>>
    ): Response<DeliveryDateResponse>

    @POST("api/v3/dbs/groups/info")
    suspend fun getGroupsInfo(
        @Header("Authorization") token: String,
        @Body body: Map<String, List<String>>
    ): Response<List<GroupInfoDto>>
}