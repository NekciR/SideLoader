package com.rickendy.sideloader.data.model

import com.google.gson.annotations.SerializedName

data class AppInfo(
    val id: Int,
    val name: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("version_name") val versionName: String,
    @SerializedName("version_code") val versionCode: Int,
    val description: String,
    @SerializedName("apk_url") val apkUrl: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val screenshots: List<String>,
    val changelogs: List<Changelog>
)

data class Changelog(
    @SerializedName("version_name") val versionName: String,
    @SerializedName("version_code") val versionCode: Int,
    val description: String,
    @SerializedName("created_at") val createdAt: String
)

data class AppCatalog(
    val apps: List<AppInfo>
)