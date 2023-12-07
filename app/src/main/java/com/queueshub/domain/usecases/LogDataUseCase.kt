package com.queueshub.domain.usecases

import arrow.core.getOrHandle
import com.queueshub.data.api.model.ApiLog
import com.queueshub.domain.model.Order
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogDataUseCase @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(data:ApiLog): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext appRepository.logData(data).getOrHandle {
                throw it
            }
        }
    }
}