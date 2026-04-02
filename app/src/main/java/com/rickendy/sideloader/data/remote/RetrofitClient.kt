package com.rickendy.sideloader.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val url = "https://gist.githubusercontent.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val appCatalogApiService: AppCatalogApiService = retrofit.create(AppCatalogApiService::class.java)
    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
}