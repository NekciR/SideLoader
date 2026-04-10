package com.rickendy.sideloader.data.remote

import com.rickendy.sideloader.data.model.AppCatalog
import retrofit2.http.GET
import retrofit2.http.Header

interface AppCatalogApiService {
    @GET("apps")
    suspend fun getCatalog(
        @Header("Authorization") token: String
    ): AppCatalog
}