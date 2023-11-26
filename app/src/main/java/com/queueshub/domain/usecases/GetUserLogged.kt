package com.queueshub.domain.usecases

import com.queueshub.domain.model.User
import com.queueshub.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserLogged @Inject constructor(private val appRepository: AppRepository) {

    operator fun invoke(): Pair<Boolean, Long?> {
           return appRepository.getUserLogged()
    }
}