package com.rickendy.sideloader.data.model

import com.google.gson.annotations.SerializedName

data class AppInfo(
    val id: String,
    val name: String,
    val description: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("apk_url") val apkUrl: String,
    @SerializedName("version_name") val versionName: String,
    @SerializedName("version_code") val versionCode: Int,
    val changelog: String,
    val screenshots: List<String>
)

data class AppCatalog(
    val apps: List<AppInfo>
)