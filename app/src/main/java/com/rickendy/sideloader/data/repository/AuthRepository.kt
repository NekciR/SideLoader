package com.rickendy.sideloader.data.repository

import com.rickendy.sideloader.data.model.User
import com.rickendy.sideloader.data.remote.RetrofitClient

object AuthRepository {

    private const val USERS_URL = "https://gist.githubusercontent.com/NekciR/29e0155594ad8e321a03fc4f991c01e4/raw/userlist.json"

    suspend fun getUsers(): Result<List<User>> {
        return try {
            val userList = RetrofitClient.authApiService.getUser(USERS_URL)
            Result.success(userList.users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): User? {
        val result = getUsers()
        if (result.isFailure) return null
        return result.getOrNull()?.find { user ->
            user.username == username && user.password == password
        }
    }
}