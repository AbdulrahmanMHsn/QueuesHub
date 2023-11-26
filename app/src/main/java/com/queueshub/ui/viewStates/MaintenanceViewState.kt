package com.queueshub.ui.viewStates

import com.queueshub.domain.model.Maintenance
import com.queueshub.domain.model.Sensor
import com.queueshub.ui.models.Event

data class MaintenanceViewState(
    val loading: Boolean = false,
    val maintenances: List<Maintenance>? = null,
    val failure: Event<Throwable>? = null
)