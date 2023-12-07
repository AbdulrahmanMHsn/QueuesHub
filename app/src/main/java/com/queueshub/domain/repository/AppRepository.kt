package com.queueshub.domain.repository

import arrow.core.Either
import com.google.gson.Gson
import com.queueshub.data.api.model.ApiLog
import com.queueshub.domain.model.*
import java.io.File
import java.io.IOException


interface AppRepository {
    fun getUserLogged(): Pair<Boolean, Long?>
    fun getOrderStarted(): Boolean
    fun shouldBePaid(): Boolean
    suspend fun setUserLogged(logged: Boolean, user: User?, token: String)
    suspend fun setOrderStarted(started: Boolean)
    suspend fun setOrderPayment(shouldBePaid: Boolean)
    suspend fun login(phone: String, pin: String): Either<IOException, User>
    suspend fun getMyOrders(id:Long): Either<IOException, List<Car>?>
    suspend fun closeOrder(orderId: Long,amount:Int): Either<IOException, Boolean>
    suspend fun getSensors(): Either<IOException, List<Sensor>>
    suspend fun getCarModels(): Either<IOException, List<String>>
    suspend fun getMaintenance(): Either<IOException, List<Maintenance>>
    suspend fun validateImei(imei:String): Either<IOException, Any>
    suspend fun getAllOrders(): Either<IOException, List<Order>?>
    suspend fun createOrder(
        carModel: String,
        year: String,
        color: String,
        chassisNum: String,
        motorNum: String,
        plateNum: String,
        oldPlateNum: String,
        carStatus: String,
        orderId: Long,
        deviceIsSupplied: String,
        deviceOldIMEI: String,
        deviceIsIMEI: String,
        deviceIsDeviceName: String,
        deviceIsDeviceId: String,
        simType: String,
        simSN: String,
        simGSM: String,
        oldSim: String,
        isSimSupplied: String,
        orderType: ArrayList<String>,
        removeDevice: String,
        removeDeviceWithWho: String,
        sensors: ArrayList<Sensor>,
        maintenances: ArrayList<Maintenance>,
        maintenanceFile: File?,
        simFile: File?,
        deviceFile: File?,
        plateFile: File?,
        frontLicenseFile: File?,
        backLicenseFile: File?,
        chassisFile: File?,
        note: String
    ): Either<IOException, Any>

    suspend fun logData(data: ApiLog): Either<IOException, Boolean>
}