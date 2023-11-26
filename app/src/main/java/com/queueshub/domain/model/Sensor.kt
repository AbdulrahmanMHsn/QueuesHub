package com.queueshub.domain.model

import java.io.File

data class Sensor(
    val id: Long,
    val name: String,
    val path: String,
    val needAttach: Boolean,
    var file: File?=null,
    var isTawreed: Boolean=true
)