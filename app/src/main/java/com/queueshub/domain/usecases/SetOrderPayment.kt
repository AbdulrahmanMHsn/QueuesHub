package com.queueshub.domain.usecases

import com.queueshub.domain.model.User
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetOrderPayment @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(shouldBePaid: Boolean) {
        return withContext(Dispatchers.IO) {
            appRepository.setOrderPayment(shouldBePaid)
        }
    }
}