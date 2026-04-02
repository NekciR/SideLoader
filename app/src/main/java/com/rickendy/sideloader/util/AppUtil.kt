package com.rickendy.sideloader.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

fun isAppInstalled(context: Context, packageId: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageId, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun getInstalledVersionCode(context: Context, packageId: String): Int {
    return try {
        val info = context.packageManager.getPackageInfo(packageId, 0)
        info.versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        -1
    }
}

fun openApp(context: Context, packageId: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageId)
    if (intent != null) {
        context.startActivity(intent)
    }
}


fun uninstallApp(context: Context, packageId: String) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = Uri.parse("package:$packageId")
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}