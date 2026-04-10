package com.rickendy.sideloader.data.remote

import com.rickendy.sideloader.data.model.AppCatalog
import com.rickendy.sideloader.data.model.LoginRequest
import com.rickendy.sideloader.data.model.LoginResponse
import com.rickendy.sideloader.data.model.RefreshRequest
import com.rickendy.sideloader.data.model.RefreshResponse
import com.rickendy.sideloader.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): RefreshResponse
}