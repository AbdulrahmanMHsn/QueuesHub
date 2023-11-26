package com.queueshub.ui.viewStates

import com.queueshub.ui.models.Event

data class ModelsViewState(
    val loading: Boolean = false,
    val models: List<String>? = null,
    val failure: Event<Throwable>? = null
)