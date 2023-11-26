package com.queueshub.data.local

import android.content.SharedPreferences
import com.queueshub.domain.model.User
import javax.inject.Inject


class Local @Inject constructor(private val sharedPreferences: SharedPreferences) : LocalSource {

    override fun getUserLogged(): Pair<Boolean, Long?> =
        Pair(sharedPreferences.getBoolean("isLogged", false), sharedPreferences.getLong("id", 0))

    override fun getOrderStarted(): Boolean =
        sharedPreferences.getBoolean("orderStarted", false)

    override fun shouldBePaid(): Boolean =
        sharedPreferences.getBoolean("orderPayment", false)

    override fun getToken(): String {
        return "Bearer ${sharedPreferences.getString("token", "")!!}"
    }

    override fun setUserLogged(logged: Boolean, user: User?, token: String) {
        val editor = sharedPreferences.edit()
        editor.putLong("id", user?.id ?: 0)
        editor.putBoolean("isLogged", logged)
        editor.putString("token", token)
        editor.apply()
    }

    override fun setOrderStarted(started: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("orderStarted", started)
        editor.apply()
    }

    override fun setOrderPayment(shouldBePaid: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("orderPayment", shouldBePaid)
        editor.apply()
    }

}