package com.queueshub.data.api.model

import com.queueshub.domain.model.Order
import com.queueshub.domain.model.OrderCount
import com.queueshub.domain.model.OrderableCount


data class ApiOrder(
    val id: Long?,
    val start_date: String?,
    val end_date: String?,
    val governorate_id: Long?,
    val address: String?,
    val needed_number: String?,
    val customer_id: Long?,
    val customer_name: String?,
    val customer_delegator: String?,
    val customer_delegator_phone: String?,
    val customer_national_id: String?,
    val number_of_cars: Int?,
    val finish_cars: Int?,
    val status_ar: String?,
    val status: String?,
    val status_date: String?,
    val order_creator: Long?,
    val in_company: Int?,
    val needed_amount: String?,
    val received_amount: String?,
    val needed_name: String?,
    val governorate: ApiGovernment?,
    val order_counts: List<ApiOrderCount>?,
    val notes: String?
    )

data class ApiOrderCount(
    val id: Long?,
    val order_id: Long?,
    val orderable_count_type: String?,
    val orderable_count_id: Long?,
    val count: Int?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?,
    val supply_count: Int?,
    val orderable_count: ApiOrderableCount?
)

data class ApiOrderableCount(
    val id: Long?,
    val name: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?
)

data class ApiOrderResource(
    val order_car: List<ApiCar>?
)
data class ApiOrdersResource(
    val order: List<ApiOrder>
)
data class ApiOrdersPagination(
    val data: List<ApiOrder>?
)


fun ApiOrder.mapToDomain(): Order {
    return Order(
        id ?: throw MappingException("Order ID cannot be null"),
        number_of_cars ?: 0,
        finish_cars ?: 0,
        start_date ?: "",
        end_date ?: "",
        governorate_id ?: 0,
        address.orEmpty(),
        needed_number.orEmpty(),
        customer_id ?: 0,
        customer_name.orEmpty(),
        customer_delegator.orEmpty(),
        customer_delegator_phone.orEmpty(),
        customer_national_id.orEmpty(),
        status_ar.orEmpty(),
        status.orEmpty(),
        order_creator ?: 0,
        in_company ?: 0,
        needed_amount.orEmpty(),
        received_amount.orEmpty(),
        needed_name.orEmpty(),
        governorate?.mapToDomain(),
        order_counts?.map { it.mapToDomain() },
        notes = notes
    )
}

fun ApiOrderCount.mapToDomain(): OrderCount {
    return OrderCount(
        id = id ?: 0,
        orderId = order_id ?: 0,
        orderableCountType = orderable_count_type.orEmpty(),
        orderableCountId = orderable_count_id ?: 0,
        count = count ?: 0,
        supplyCount = supply_count ?: 0,
        orderableCount = orderable_count?.mapToDomain()
    )
}

fun ApiOrderableCount.mapToDomain(): OrderableCount {
    return OrderableCount(
        id = id ?: 0,
        name = name.orEmpty()
    )
}