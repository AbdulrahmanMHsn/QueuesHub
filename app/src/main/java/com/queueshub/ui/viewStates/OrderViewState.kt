package com.queueshub.ui.viewStates

import com.queueshub.domain.model.Car
import com.queueshub.domain.model.Order
import com.queueshub.ui.models.Event
import com.queueshub.domain.model.User

data class OrderViewState(
    val loading: Boolean = false,
    val orders: List<Order>? = null,
    val failure: Event<Throwable>? = null
)

data class CarsViewState(
    val loading: Boolean = false,
    val cars: List<Car>? = null,
    val failure: Event<Throwable>? = null
)

data class CreateOrderViewState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val failure: Event<Throwable>? = null
)