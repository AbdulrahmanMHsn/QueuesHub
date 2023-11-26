package com.queueshub.data.api.model

import com.queueshub.domain.model.Car
import com.queueshub.domain.model.Order


data class ApiCar(
    val id: Long?,
    val start_date: String?,
    val status_ar: String?,
    val status: String?,
    val status_date: String?,
    val motor_num: String?,
    val chassis_num: String?,
    val plate_num: String?,

    )


fun ApiCar.mapToDomain(): Car {
    return Car(
        id ?: throw MappingException("Order ID cannot be null"),

        start_date ?: "",

        status_ar.orEmpty(),
        status.orEmpty(),
        status_date.orEmpty(),
        motor_num,
        chassis_num,
        plate_num
    )
}