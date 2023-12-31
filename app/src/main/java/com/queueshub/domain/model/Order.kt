package com.queueshub.domain.model

data class Order(
    val id: Long,
    val numberOfCars: Int,
    val finishedCars: Int,
    val startDate: String,
    val endDate: String,
    val governorateId: Long,
    val address: String,
    val neededNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerDelegator: String,
    val customerDelegatorPhone: String,
    val customerNationalId: String,
    val statusAr: String,
    val status: String,
    val orderCreator: Long,
    val inCompany: Int,
    val neededAmount: String,
    val receivedAmount: String,
    val neededName: String,
    val governorate: Government?
)