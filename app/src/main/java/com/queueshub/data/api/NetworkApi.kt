package com.queueshub.data.api

import com.queueshub.data.api.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface NetworkApi {

    @POST(ApiConstants.LOGIN_ENDPOINT)
    suspend fun login(
        @Query("phone") phone: String,
        @Query("pin") pin: String,
    ): ApiContainer<ApiLoginResource>

    @GET(ApiConstants.CURRENT_ENDPOINT)
    suspend fun getMyCurrentOrders(
        @Header("Authorization") authHeader: String,
        @Query("order_id") id:Long,
    ): ApiContainer<ApiOrderResource>

    @GET(ApiConstants.MY_ORDERS_ENDPOINT)
    suspend fun getAllOrders(
        @Header("Authorization") authHeader: String,
    ): ApiContainer<ApiOrdersResource>

    @FormUrlEncoded
    @POST(ApiConstants.SENSORS_ENDPOINT)
    suspend fun getAllSensors(
        @Header("Authorization") authHeader: String,
        @Field("page") page: Int = 1,
        @Field("page_size") size: Int = 100,
        @Field("filters[]") filters: String = "",
        ): ApiContainer<ApiSensorResource>

    @FormUrlEncoded
    @POST(ApiConstants.CARS_TYPES_ENDPOINT)
    suspend fun getCarsTypes(
        @Header("Authorization") authHeader: String,
        @Field("page") page: Int = 1,
        @Field("page_size") size: Int = 100,
        @Field("filters[]") filters: String = "",
        ): ApiContainer<ApiCarResource>

    @FormUrlEncoded
    @POST(ApiConstants.MAINTENANCE_ENDPOINT)
    suspend fun getMaintenances(
        @Header("Authorization") authHeader: String,
        @Field("page") page: Int = 1,
        @Field("page_size") size: Int = 100,
        @Field("filters[]") filters: String = "",
        ): ApiContainer<ApiMaintenanceResource>

    @Multipart
    @POST(ApiConstants.CREATE_ORDER_ENDPOINT)
    suspend fun createOrder(
        @Header("Authorization") authHeader: String,
        @PartMap() partMap: MutableMap<String, RequestBody?>,
        @Part attachments: List<MultipartBody.Part?>,
        ): ApiContainer<ApiSensorResource>

    @FormUrlEncoded
    @POST(ApiConstants.CLOSE_ORDER_ENDPOINT)
    suspend fun closeOrder(
        @Header("Authorization") authHeader: String,
        @Field("id") orderId: Long = 1,
        @Field("received_amount") received_amount: Int = 0,
        ): Response<Unit>

    fun validateImei(token: String, imei: String): Any {
        TODO("Not yet implemented")
    }

}