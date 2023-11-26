package com.queueshub.data.api.model

import com.queueshub.domain.model.Maintenance
import com.queueshub.domain.model.Sensor


data class ApiMaintenance(
    val id: Long?,
    val name: String?,
    val path: String?,
    val need_description: Int?,
)

data class ApiMaintenancePagination(
    val current_page: Int?,
    val data: ArrayList<ApiMaintenance>,
)

data class ApiMaintenanceResource(
    val maintenance: ApiMaintenancePagination
)

fun ApiMaintenance.mapToDomain(): Maintenance {
    return Maintenance(
        id ?: throw MappingException("Sensor ID cannot be null"),
        name.orEmpty(),
        "",
        needsDescription = need_description == 1
    )
}

data class RequestMaintenance(val id: Int, val name: String)