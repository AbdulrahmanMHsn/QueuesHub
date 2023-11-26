package com.queueshub.ui.viewStates

import com.queueshub.ui.models.Event
import com.queueshub.domain.model.User

data class LoginViewState(
    val loading: Boolean = false,
    val user: User? = null,
    val failure: Event<Throwable>? = null
)