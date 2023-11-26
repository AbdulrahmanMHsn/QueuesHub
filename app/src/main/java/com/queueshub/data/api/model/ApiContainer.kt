package com.queueshub.data.api.model

import com.google.gson.annotations.SerializedName


data class ApiContainer<T>(
    @SerializedName("data")  val data: T
)
