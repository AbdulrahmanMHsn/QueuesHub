package com.queueshub.domain.model

data class Car(
    val id: Long,
    val startDate: String,
    val statusAr: String,
    val status: String,
    val statusDate: String,
    val motorNum: String?,
    val chassisNum: String?,
    val plateNum: String?,
)