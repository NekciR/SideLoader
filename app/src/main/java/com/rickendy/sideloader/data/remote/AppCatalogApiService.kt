package com.rickendy.sideloader.data.remote

import com.rickendy.sideloader.data.model.AppCatalog
import retrofit2.http.GET
import retrofit2.http.Url

interface AppCatalogApiService {
    @GET
    suspend fun getCatalog(@Url url: String): AppCatalog
}