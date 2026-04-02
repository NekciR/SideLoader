package com.rickendy.sideloader.data.repository

import com.rickendy.sideloader.data.model.AppCatalog
import com.rickendy.sideloader.data.remote.RetrofitClient

object AppRepository {

    private const val CATALOG_URL = "https://gist.githubusercontent.com/NekciR/eac99462dde22d1ae7db58e0157a2fa9/raw/applist.json"

    suspend fun getApps(): Result<AppCatalog> {
        return try {
            val catalog = RetrofitClient.appCatalogApiService.getCatalog(CATALOG_URL)
            Result.success(catalog)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}