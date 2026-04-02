package com.rickendy.sideloader.util

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

sealed class DownloadResult {
    object Success : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

suspend fun downloadAndInstall(
    context: Context,
    apkUrl: String,
    appName: String,
    onProgress: (Int) -> Unit
): DownloadResult {
    return withContext(Dispatchers.IO) {
        try {
            val apkFile = File(context.cacheDir, "$appName.apk")

            val connection = URL(apkUrl).openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            val totalSize = connection.contentLength
            var downloadedSize = 0

            connection.getInputStream().use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        downloadedSize += bytes
                        if (totalSize > 0) {
                            val progress = (downloadedSize * 100 / totalSize)
                            onProgress(progress)
                        }
                        bytes = input.read(buffer)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }

            DownloadResult.Success

        } catch (e: java.net.SocketTimeoutException) {
            DownloadResult.Error("Connection timed out. Check your internet connection.")
        } catch (e: java.net.UnknownHostException) {
            DownloadResult.Error("Could not reach server. Check your internet connection.")
        } catch (e: java.io.IOException) {
            DownloadResult.Error("Download failed: ${e.message}")
        } catch (e: Exception) {
            DownloadResult.Error("Unexpected error: ${e.message}")
        }
    }
}

private fun installApk(context: Context, apkFile: File) {
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )
    } else {
        android.net.Uri.fromFile(apkFile)
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}