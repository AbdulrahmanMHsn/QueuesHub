package com.queueshub.data.api.model

import com.queueshub.domain.model.Sensor


data class ApiSensor(
    val id: Long?,
    val name: String?,
    val path: String?,
    val is_need_attachment: Int = 0,
)

data class ApiSensorPagination(
    val current_page: Int?,
    val data: ArrayList<ApiSensor>,
)

data class ApiSensorResource(
    val sensor: ApiSensorPagination
)


fun ApiSensor.mapToDomain(): Sensor {
    return Sensor(
        id ?: throw MappingException("Sensor ID cannot be null"),
        name.orEmpty(),
        path.orEmpty(),
        is_need_attachment == 1,
        file = null,
        true
    )
}

data class RequestSensor(val is_supplied: Int, val sensor_name: String, val sensor_id: Int)