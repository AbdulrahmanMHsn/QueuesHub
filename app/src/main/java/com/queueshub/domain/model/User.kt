package com.queueshub.domain.model

data class User(
    val id: Long,
    val name: String,
    val phone: String,
    val username: String,
    val type: String,
    val need_chng_pass: Int
)