package com.queueshub.data.local

import com.queueshub.domain.model.User

interface LocalSource {
    fun getUserLogged(): Pair<Boolean, Long?>
    fun getOrderStarted(): Boolean
    fun shouldBePaid(): Boolean
    fun getToken(): String
    fun setUserLogged(logged: Boolean, user: User?, token: String)
    fun setOrderStarted(started: Boolean)
    fun setOrderPayment(shouldBePaid: Boolean)
}