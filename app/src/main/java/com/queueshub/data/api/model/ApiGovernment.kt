package com.queueshub.data.api.model

import com.queueshub.domain.model.Government
import com.queueshub.domain.model.Order

data class ApiGovernment(
    val id: Long,
    val name: String
)


fun ApiGovernment.mapToDomain(): Government {
    return Government(id, name)
}

