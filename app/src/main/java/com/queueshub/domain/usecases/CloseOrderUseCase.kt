package com.queueshub.domain.usecases

import arrow.core.getOrHandle
import com.queueshub.domain.model.Order
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CloseOrderUseCase @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(orderId: Long,amount:Int): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext appRepository.closeOrder(orderId,amount).getOrHandle {
                throw it
            }
        }
    }
}