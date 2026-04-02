package com.rickendy.sideloader.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val username: String,
    val password: String,
    @SerializedName("display_name") val displayName: String
)

data class UserList(
    val users: List<User>
)