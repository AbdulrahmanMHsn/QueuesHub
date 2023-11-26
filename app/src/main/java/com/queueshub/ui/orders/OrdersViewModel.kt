package com.queueshub.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queueshub.domain.model.Order
import com.queueshub.domain.usecases.*
import com.queueshub.ui.models.Event
import com.queueshub.ui.viewStates.CarsViewState
import com.queueshub.ui.viewStates.OrderViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    val getUserOrder: GetUserOrder,
    val closeOrderUseCase: CloseOrderUseCase,
    val getOrderStarted: GetOrderStarted,
    val setOrderStarted: SetOrderStarted,
    val setOrderPayment: SetOrderPayment,
    val shouldBePaid: ShouldBePaidUseCase,
    val getAllOrders: GetAllOrders
) :
    ViewModel() {

    var receivedAmount: Int = 0
    val shouldPaid = shouldBePaid()
    private val _state = MutableStateFlow(OrderViewState())
    val state: StateFlow<OrderViewState> = _state.asStateFlow()
    private val _carsState = MutableStateFlow(CarsViewState())
    val carsState: StateFlow<CarsViewState> = _carsState.asStateFlow()
    var orderStarted: Boolean = false
    fun startOrder() {
        viewModelScope.launch {
            setOrderStarted(true)
        }
    }

    fun getOrders(order: Order) {
        orderStarted = getOrderStarted()
        viewModelScope.launch {
            _carsState.update {
                it.copy(loading = true)
            }
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    _carsState.update {
                        try {
                            val cars = getUserOrder(order.id)
                            setOrderPayment((order.neededAmount.toFloat()) > 0f)
                            it.copy(loading = false, cars = cars)
                        } catch (e: IOException) {
                            it.copy(loading = false, failure = Event(e))
                        }
                    }
                }
            }
        }
    }

    fun fetchAllOrders() {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    _state.update {
                        try {
                            val order = getAllOrders()

                            it.copy(loading = false, orders = order)
                        } catch (e: IOException) {
                            it.copy(loading = false, failure = Event(e))
                        }
                    }
                }
            }
        }
    }

    fun closeOrder(orderId: Long) {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _state.update {
                    try {
                        closeOrderUseCase(orderId, receivedAmount)
                        it.copy(loading = false, orders = null)
                    } catch (e: IOException) {
                        it.copy(loading = false, failure = Event(e))
                    }
                }
            }
        }
    }
}