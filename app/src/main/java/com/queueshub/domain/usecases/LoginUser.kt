package com.queueshub.domain.usecases

import arrow.core.Either
import arrow.core.getOrHandle
import com.queueshub.domain.model.NetworkException
import com.queueshub.domain.model.User
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class LoginUser @Inject constructor(private val appRepository: AppRepository) {

    suspend operator fun invoke(phone: String, pin: String): User {
        return withContext(Dispatchers.IO) {
            appRepository.login(phone, pin).getOrHandle {
                throw it
            }
        }
    }
}