package com.queueshub.data

import arrow.core.Either
import com.google.gson.Gson
import com.queueshub.data.api.NetworkApi
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.mapToDomain
import com.queueshub.data.local.LocalSource
import com.queueshub.domain.model.*
import com.queueshub.domain.repository.AppRepository
import com.queueshub.utils.createPartFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject


class AppRepositoryImp @Inject constructor(
    private val api: NetworkApi, private val local: LocalSource
) : AppRepository {


    override fun getUserLogged() = local.getUserLogged()
    override fun getOrderStarted() = local.getOrderStarted()
    override fun shouldBePaid() = local.shouldBePaid()

    override suspend fun setUserLogged(logged: Boolean, user: User?, token: String) {
        local.setUserLogged(logged, user, token)
    }

    override suspend fun setOrderStarted(started: Boolean) {
        local.setOrderStarted(started)
    }

    override suspend fun setOrderPayment(shouldBePaid: Boolean) {
        local.setOrderPayment(shouldBePaid)
    }

    override suspend fun login(phone: String, pin: String): Either<IOException, User> {
        return try {
            val results = api.login(phone, pin)
            val user = results.data.user.mapToDomain()
            setUserLogged(true, user, results.data.token)
            Either.Right(user)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun closeOrder(orderId: Long, amount: Int): Either<IOException, Boolean> {
        return try {
            api.closeOrder(local.getToken(), orderId, amount)
            Either.Right(true)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun logData(data: ApiLog): Either<IOException, Boolean> {
        return try {
            api.logData(local.getToken(), data)
            Either.Right(true)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }
    override suspend fun getMyOrders(id: Long): Either<IOException, List<Car>?> {
        return try {
            val results = api.getMyCurrentOrders(local.getToken(), id)
            val cars = results.data.order_car?.map { it.mapToDomain() }
            Either.Right(cars)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun getAllOrders(): Either<IOException, List<Order>?> {
        return try {
            val results = api.getAllOrders(local.getToken())
            val order = results.data.order.map { it.mapToDomain() }
            Either.Right(order)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun getSensors(): Either<IOException, List<Sensor>> {
        return try {
            val results = api.getAllSensors(local.getToken())
            val order = results.data.sensor.data.map { it.mapToDomain() }
            Either.Right(order)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun getCarModels(): Either<IOException, List<String>> {
        return try {
            val results = api.getCarsTypes(local.getToken())
            val order = results.data.car_model.data.map { it.mapToDomain() }
            Either.Right(order)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun getMaintenance(): Either<IOException, List<Maintenance>> {
        return try {
            val results = api.getMaintenances(local.getToken())
            val order = results.data.maintenance.data.map { it.mapToDomain() }
            Either.Right(order)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun validateImei(imei: String): Either<IOException, Any> {
        return try {
            val results = api.validateImei(local.getToken(), imei)
            Either.Right(true)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override suspend fun createOrder(
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
    ): Either<IOException, Any> {
        val map: MutableMap<String, RequestBody?> = mutableMapOf()
        val carModelPart = createPartFromString(carModel)
        map["model"] = carModelPart
        val yearPart = createPartFromString(year)
        map["year"] = yearPart
        val colorPart = createPartFromString(color)
        map["color"] = colorPart
        val chassisNumPart = createPartFromString(chassisNum)
        map["chassis num"] = chassisNumPart
        val motorNumPart = createPartFromString(motorNum)
        map["motor_num"] = motorNumPart
        val plateNumPart = createPartFromString(plateNum)
        map["plate_num"] = plateNumPart
        val oldPlateNumPart = createPartFromString(oldPlateNum)
        map["old_plate_num"] = oldPlateNumPart
        val carStatusPart = createPartFromString(carStatus)
        map["car_status"] = carStatusPart
        val orderIdPart = createPartFromString(orderId.toString())
        map["order_id"] = orderIdPart
        val technicalNotePart = createPartFromString(note)
        map["technical_note"] = technicalNotePart
        val deviceIsSuppliedPart = createPartFromString(deviceIsSupplied)
        map["device[is_supplied]"] = deviceIsSuppliedPart
        val deviceIsIMEIPart = createPartFromString(deviceIsIMEI)
        map["device[imei]"] = deviceIsIMEIPart
        val deviceOldIMEIPart = createPartFromString(deviceOldIMEI)
        map["device[old_imei]"] = deviceOldIMEIPart
        val deviceIsDeviceNamePart = createPartFromString(deviceIsDeviceName)
        map["device[device_name]"] = deviceIsDeviceNamePart
        val deviceIdPart = createPartFromString("")
        map["device[device_id]"] = deviceIdPart
        val serialNumPart = createPartFromString(deviceIsDeviceId)
        map["device[serial_num]"] = serialNumPart
        val imageList = mutableListOf<MultipartBody.Part?>()
        if (removeDevice.isNotEmpty()) {
            val removeDevicePart = createPartFromString(removeDevice)
            map["remove_device[status]"] = removeDevicePart
            val removeDeviceWithWhoPart = createPartFromString(removeDeviceWithWho)
            map["remove_device[with_whom]"] = removeDeviceWithWhoPart
        } else {
            val removeDevicePart = createPartFromString("")
            map["remove_device"] = removeDevicePart
        }
        if (sensors.isNotEmpty()) {
            sensors.forEachIndexed { index, it ->
                val sensorIdPart = createPartFromString(it.id.toString())
                map["sensors[$index][sensor_id]"] = sensorIdPart
                val sensorNamePart = createPartFromString(it.name)
                map["sensors[$index][sensor_name]"] = sensorNamePart
                map["sensors[$index][is_supplied]"] =
                    createPartFromString(if (it.isTawreed) "1" else "0")
                if (it.file != null) {
                    map["sensors[$index][attachment][type]"] = createPartFromString("sensor")
                    map["sensors[$index][attachment][attachmentable_type]"] =
                        createPartFromString("order_car_sensors")
                    val requestFile: RequestBody = RequestBody.create(
                        "image/jpg".toMediaType(), it.file!!
                    )
                    val sensorImage = MultipartBody.Part.createFormData(
                        "sensors[$index][attachment][file]",
                        "${it.file!!.nameWithoutExtension}.png",
                        requestFile
                    )
                    imageList.add(sensorImage)
                }
            }
        } else {

            val sensorsPart = createPartFromString("")
            map["sensors"] = sensorsPart
        }
        if (maintenances.isNotEmpty()) {
            maintenances.forEachIndexed { index, it ->
                val maintenanceIdPart = createPartFromString(it.id.toString())
                map["maintenance_header[maintenance_details][$index][maintenance_id]"] =
                    maintenanceIdPart
                val maintenanceNamePart = createPartFromString(it.name)
                map["maintenance_header[maintenance_details][$index][name]"] = maintenanceNamePart
                val maintenanceDescPart = createPartFromString(it.description)
                map["maintenance_header[maintenance_details][$index][description]"] =
                    maintenanceDescPart
            }
        } else {
            val maintenancePart = createPartFromString("")
            map["maintenance_header"] = maintenancePart
        }
        val simTypePart = createPartFromString(simType)
        map["sim[sim_type]"] = simTypePart
        val simSerialPart = createPartFromString(simSN)
        map["sim[serial_num]"] = simSerialPart
        val simGSMPart = createPartFromString(simGSM)
        map["sim[sim_num]"] = simGSMPart
        val simOldPart = createPartFromString(oldSim)
        map["sim[old_serial_num]"] = simOldPart
        val isSimSuppliedPart = createPartFromString(isSimSupplied)
        map["sim[is_supplied]"] = isSimSuppliedPart
        val simattachmentPart = createPartFromString("sim")
        orderType.forEachIndexed { index, it ->
            val orderTypePart = createPartFromString(it)
            map["types[$index][type]"] = orderTypePart
        }
        val PlateNumAttPart = createPartFromString("plate_num")
        var maintenanceImage: MultipartBody.Part? = null

        if (maintenanceFile != null) {

            val maintenanceHeadPart = createPartFromString("maintenance_header")
            map["maintenance_header[attachment][type]"] = maintenanceHeadPart
            val maintenanceattachmentablePart =
                createPartFromString("order_car_header_maintenances")
            map["maintenance_header[attachment][attachmentable_type]"] =
                maintenanceattachmentablePart
            val requestFile: RequestBody = RequestBody.create(
                "image/jpg".toMediaType(), maintenanceFile
            )
            maintenanceImage = MultipartBody.Part.createFormData(
                "maintenance_header[attachment][file]",
                "${maintenanceFile.nameWithoutExtension}.png",
                requestFile
            )
        } else if (orderType.contains("صيانة")) {
            val maintenanceHeadPart = createPartFromString("")
            map["maintenance_header[attachment]"] = maintenanceHeadPart
        }

        imageList.add(maintenanceImage)
        var simImage: MultipartBody.Part? = null
        simFile?.let { file ->

            map["sim[attachment][type]"] = simattachmentPart
            val simattachmentablePart = createPartFromString("order_car_sims")
            map["sim[attachment][attachmentable_type]"] = simattachmentablePart
            val requestFile: RequestBody = RequestBody.create(
                "image/jpg".toMediaType(), file
            )
            simImage = MultipartBody.Part.createFormData(
                "sim[attachment][file]", "${file.nameWithoutExtension}.png", requestFile
            )
        }
        imageList.add(simImage)
        var deviceImage: MultipartBody.Part? = null
        deviceFile?.let { file ->

            val deviceAttachmentTypePart = createPartFromString("device")
            map["device[attachment][type]"] = deviceAttachmentTypePart
            val deviceAttachmentableTypePart = createPartFromString("order_car_devices")
            map["device[attachment][attachmentable_type]"] = deviceAttachmentableTypePart
            val requestFile: RequestBody = RequestBody.create(
                "image/jpg".toMediaType(), file
            )
            deviceImage = MultipartBody.Part.createFormData(
                "device[attachment][file]", "${file.nameWithoutExtension}.png", requestFile
            )
        }
        imageList.add(deviceImage)

        var index = 0

        if (plateFile != null) {
            map["attachments[$index][type]"] = PlateNumAttPart
            val PlateNumAttablePart = createPartFromString("order_cars")
            map["attachments[$index][attachmentable_type]"] = PlateNumAttablePart

            var plateImage: MultipartBody.Part? = null
            val requestFile: RequestBody = RequestBody.create(
                "image/jpg".toMediaType(), plateFile
            )
            plateImage = MultipartBody.Part.createFormData(
                "attachments[$index][file]", "${plateFile.nameWithoutExtension}.png", requestFile
            )
            imageList.add(plateImage)
            index++
        }
        if (frontLicenseFile != null) {
            var frontLicenseImage: MultipartBody.Part? = null
            val frontLicenseAttPart = createPartFromString("front_licene")
            map["attachments[$index][type]"] = frontLicenseAttPart
            val frontLicenseAttablePart = createPartFromString("order_cars")
            map["attachments[$index][attachmentable_type]"] = frontLicenseAttablePart
            val requestFile: RequestBody = RequestBody.create(
                "image/jpg".toMediaType(), frontLicenseFile
            )
            frontLicenseImage = MultipartBody.Part.createFormData(
                "attachments[$index][file]",
                "${frontLicenseFile.nameWithoutExtension}.png",
                requestFile
            )
            imageList.add(frontLicenseImage)
            index++
        }
        if (chassisFile != null) {
            var chassisImage: MultipartBody.Part? = null
            val chassisAttPart = createPartFromString("chassis_number")
            map["attachments[$index][type]"] = chassisAttPart
            val frontLicenseAttablePart = createPartFromString("order_cars")
            map["attachments[$index][attachmentable_type]"] = frontLicenseAttablePart
            val requestFile: RequestBody = RequestBody.create(
                "image/jpg".toMediaType(), chassisFile
            )
            chassisImage = MultipartBody.Part.createFormData(
                "attachments[$index][file]",
                "${chassisFile.nameWithoutExtension}.png",
                requestFile
            )
            imageList.add(chassisImage)
            index++
        }
        if (backLicenseFile != null) {
            var backLicenseImage: MultipartBody.Part? = null
            val backLicenseAttPart = createPartFromString("back_licene")
            map["attachments[$index][type]"] = backLicenseAttPart
            val backLicenseAttablePart = createPartFromString("order_cars")
            map["attachments[$index][attachmentable_type]"] = backLicenseAttablePart


            val requestFile: RequestBody = RequestBody.create(
                "image/jpg".toMediaType(), backLicenseFile
            )
            backLicenseImage = MultipartBody.Part.createFormData(
                "attachments[$index][file]",
                "${backLicenseFile.nameWithoutExtension}.png",
                requestFile
            )
            index++
            imageList.add(backLicenseImage)
        }
        if (index == 0) {
            val emptyAttach = createPartFromString("")
            map["attachments"] = emptyAttach
        }
        return try {
            val results = api.createOrder(local.getToken(), map, imageList)
            Either.Right(results)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }



}