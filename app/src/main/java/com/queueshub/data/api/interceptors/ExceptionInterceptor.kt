package com.queueshub.data.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.*


class ExceptionInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        if (response.code == 422) {
            val responseSt = response.body?.string()
            val jsonObj = JSONObject(responseSt!!)
            val jsonArray = jsonObj.getJSONArray("Error")
            val lang = getLanguage()
            val err = jsonArray.getString(0)
            if (err.contains("Token"))
                GlobalNavigator.logout()
            throw IOException(jsonArray.getString(0))
        }
        return response
    }

    private fun getLanguage(): String {
        return Locale.getDefault().language

    }
}