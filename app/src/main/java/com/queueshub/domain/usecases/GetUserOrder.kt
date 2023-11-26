package com.queueshub.domain.usecases

import arrow.core.getOrHandle
import com.queueshub.domain.model.Car
import com.queueshub.domain.model.Order
import com.queueshub.domain.model.User
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserOrder @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(id:Long): List<Car>? {
        return withContext(Dispatchers.IO) {
            return@withContext appRepository.getMyOrders(id).getOrHandle {
                throw it
            }
        }
    }
}