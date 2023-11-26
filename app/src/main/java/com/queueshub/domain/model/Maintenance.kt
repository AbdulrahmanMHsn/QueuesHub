package com.queueshub.domain.model

data class Maintenance(
    val id: Long,
    val name: String,
    val description: String,
    val needsDescription: Boolean,
)