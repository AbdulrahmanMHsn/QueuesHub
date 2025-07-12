package com.queueshub.domain.usecases

import arrow.core.getOrHandle
import com.queueshub.domain.model.Maintenance
import com.queueshub.domain.model.Order
import com.queueshub.domain.model.Sensor
import com.queueshub.domain.model.User
import com.queueshub.domain.repository.AppRepository
import com.queueshub.utils.ArabicToEnglish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class CreateOrder @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(
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
        shasisFile: File?,
        note: String,
        technical_start: String,
    ) {
        return withContext(Dispatchers.IO) {
            appRepository.createOrder(
                carModel,
                year.ArabicToEnglish(),
                color,
                chassisNum.ArabicToEnglish(),
                motorNum.ArabicToEnglish(),
                plateNum.ArabicToEnglish(),
                oldPlateNum.ArabicToEnglish(),
                carStatus,
                orderId,
                deviceIsSupplied,
                deviceOldIMEI.ArabicToEnglish(),
                deviceIsIMEI.ArabicToEnglish(),
                deviceIsDeviceName,
                deviceIsDeviceId,
                simType,
                simSN.ArabicToEnglish(),
                simGSM.ArabicToEnglish(),
                oldSim.ArabicToEnglish(),
                isSimSupplied,
                orderType,
                removeDevice,
                removeDeviceWithWho,
                sensors,
                maintenances,
                maintenanceFile,
                simFile,
                deviceFile,
                plateFile,
                frontLicenseFile,
                backLicenseFile,
                shasisFile,
                note,
                technical_start
            ).getOrHandle {
                throw it
            }
        }
    }
}