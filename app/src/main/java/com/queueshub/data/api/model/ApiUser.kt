package com.queueshub.data.api.model

import com.queueshub.domain.model.User


data class ApiUser(
    val id: Long?,
    val name: String?,
    val phone: String?,
    val username: String?,
    val type: String?,
    val pin: String?,
    val need_chng_pass: Int?
)

data class ApiLoginResource(
    val user: ApiUser,
    val token: String
)

fun ApiUser.mapToDomain(): User {
    return User(
        id ?: throw MappingException("User ID cannot be null"),
        name.orEmpty(),
        phone.orEmpty(),
        username.orEmpty(),
        type.orEmpty(),
        need_chng_pass ?: 0
    )
}