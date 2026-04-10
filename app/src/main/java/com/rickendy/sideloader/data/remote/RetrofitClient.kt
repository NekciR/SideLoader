package com.rickendy.sideloader.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val url = "https://sideloader-api-production.up.railway.app/"

    private var accessToken: String? = null
    private var refreshToken: String? = null
    var onTokenRefreshed: ((String) -> Unit)? = null
    var onSessionExpired: (() -> Unit)? = null

    fun updateTokens(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(
            getAccessToken = { accessToken },
            getRefreshToken = { refreshToken },
            onTokenRefreshed = { newToken ->
                accessToken = newToken
                onTokenRefreshed?.invoke(newToken)
            },
            onSessionExpired = {
                onSessionExpired?.invoke()
            }
        ))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val appCatalogApiService: AppCatalogApiService = retrofit.create(AppCatalogApiService::class.java)
    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
}