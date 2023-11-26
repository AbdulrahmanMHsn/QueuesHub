package com.queueshub.data.api.model


data class ApiCarModel(
    val id: Long?,
    val name: String?,
)

data class ApiCarPagination(
    val current_page: Int?,
    val data: ArrayList<ApiCarModel>,
)

data class ApiCarResource(
    val car_model: ApiCarPagination
)


fun ApiCarModel.mapToDomain(): String {
    return name.orEmpty()
}
