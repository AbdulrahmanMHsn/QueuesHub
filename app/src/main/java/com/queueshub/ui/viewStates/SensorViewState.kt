package com.queueshub.ui.viewStates

import com.queueshub.domain.model.Sensor
import com.queueshub.ui.models.Event

data class SensorViewState(
    val loading: Boolean = false,
    val sensors: List<Sensor>? = null,
    val failure: Event<Throwable>? = null
)