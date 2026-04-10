package com.rickendy.sideloader.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = inputFormat.parse(isoDate)
        val formattedDate = outputFormat.format(date!!)
        val timezoneLabel = getIndonesianTimezone()
        "$formattedDate $timezoneLabel"
    } catch (e: Exception) {
        isoDate
    }
}

fun getIndonesianTimezone(): String {
    return when (TimeZone.getDefault().id) {
        "Asia/Jakarta",
        "Asia/Pontianak" -> "WIB"
        "Asia/Makassar",
        "Asia/Balikpapan" -> "WITA"
        "Asia/Jayapura" -> "WIT"
        else -> TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
    }
}