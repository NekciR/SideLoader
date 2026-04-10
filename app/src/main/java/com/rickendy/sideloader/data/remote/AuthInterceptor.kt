package com.rickendy.sideloader.data.remote

import com.rickendy.sideloader.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val getAccessToken: () -> String?,
    private val getRefreshToken: () -> String?,
    private val onTokenRefreshed: (String) -> Unit,
    private val onSessionExpired: () -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${getAccessToken()}")
            .build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            val refreshToken = getRefreshToken() ?: run {
                onSessionExpired()
                return response
            }

            val refreshResult = runBlocking {
                AuthRepository.refreshToken(refreshToken)
            }

            return if (refreshResult.isSuccess) {
                val newToken = refreshResult.getOrNull()!!.accessToken
                onTokenRefreshed(newToken)
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $newToken")
                        .build()
                )
            } else {
                onSessionExpired()
                response
            }
        }

        return response
    }
}