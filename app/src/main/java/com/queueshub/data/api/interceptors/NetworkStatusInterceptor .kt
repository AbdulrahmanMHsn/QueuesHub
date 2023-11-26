package com.queueshub.data.api.interceptors

import com.queueshub.data.api.ConnectionManager
import com.queueshub.domain.model.NetworkUnavailableException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class NetworkStatusInterceptor @Inject constructor(private val connectionManager: ConnectionManager) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (connectionManager.isConnected()) {
            try {
                chain.proceed(chain.request())
            } catch (e: IOException) {
                throw e
            }
        } else {
            throw NetworkUnavailableException()
        }
    }
}