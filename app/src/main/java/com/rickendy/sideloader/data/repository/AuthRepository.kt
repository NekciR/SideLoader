package com.rickendy.sideloader.data.repository

import com.rickendy.sideloader.data.model.LoginRequest
import com.rickendy.sideloader.data.model.LoginResponse
import com.rickendy.sideloader.data.model.RefreshRequest
import com.rickendy.sideloader.data.model.RefreshResponse
import com.rickendy.sideloader.data.remote.RetrofitClient

object AuthRepository {

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.authApiService.login(
                LoginRequest(username, password)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<RefreshResponse> {
        return try {
            val response = RetrofitClient.authApiService.refreshToken(
                RefreshRequest(refreshToken)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}