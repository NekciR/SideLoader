package com.rickendy.sideloader.data.repository

import com.rickendy.sideloader.data.model.AppCatalog
import com.rickendy.sideloader.data.remote.RetrofitClient

object AppRepository {

    suspend fun getApps(accessToken: String): Result<AppCatalog> {
        return try {
            val catalog = RetrofitClient.appCatalogApiService.getCatalog(
                token = "Bearer $accessToken"
            )
            Result.success(catalog)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}