package com.rickendy.sideloader.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val username: String,
    val displayName: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshResponse(
    @SerializedName("access_token") val accessToken: String,
)
