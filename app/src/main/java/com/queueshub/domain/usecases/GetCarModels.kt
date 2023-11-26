package com.queueshub.domain.usecases

import arrow.core.getOrHandle
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCarModels @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(): List<String> {
        return withContext(Dispatchers.IO) {
            return@withContext appRepository.getCarModels().getOrHandle {
                throw it
            }
        }
    }
}