package com.queueshub.utils

enum class OrderType(val value: String) {
    NEW_DEVICE("0L"),
    REPLACE_DEVICE("1L"),
    REPAIR_DEVICE("2L"),
    REMOVE_DEVICE("3L"),
    REPLACE_SIM("4L"),
    REPLACE_PLATE("5L"),
    UNDEFINED("-1L")
}

enum class CameraType {
    SENSOR,
    CAR_PLATE,
    CAR_LICENSE,
    CAR_LICENSE2,
    DEVICE,
    SHASIS,
    DEVICE_CAPTURE,
    SIM,
    SIM_CAPTURE,
    MAINTENANCE,
}

const val LICENSE_KEY = "license"

const val WITH_CLIENT = "customer"
const val WITH_INFINITY = "infinity"