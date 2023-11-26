package com.queueshub.domain.usecases

import arrow.core.getOrHandle
import com.queueshub.domain.model.Maintenance
import com.queueshub.domain.model.Order
import com.queueshub.domain.model.Sensor
import com.queueshub.domain.model.User
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetMaintenances @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(): List<Maintenance> {
        return withContext(Dispatchers.IO) {
            return@withContext appRepository.getMaintenance().getOrHandle {
                throw it
            }
        }
    }
}