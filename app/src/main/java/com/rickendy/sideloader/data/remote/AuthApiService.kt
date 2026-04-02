package com.rickendy.sideloader.data.remote

import com.rickendy.sideloader.data.model.AppCatalog
import com.rickendy.sideloader.data.model.User
import com.rickendy.sideloader.data.model.UserList
import retrofit2.http.GET
import retrofit2.http.Url

interface AuthApiService {
    @GET
    suspend fun getUser(@Url url: String): UserList
}