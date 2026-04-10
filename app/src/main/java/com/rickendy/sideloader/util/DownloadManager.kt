package com.rickendy.sideloader.util

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.rickendy.sideloader.worker.DownloadWorker
import java.util.UUID

object DownloadManager {

    private const val QUEUE_NAME = "download_queue"

    fun enqueueDownload(
        context: Context,
        apkUrl: String,
        appName: String
    ): UUID {
        val inputData = Data.Builder()
            .putString(DownloadWorker.KEY_APK_URL, apkUrl)
            .putString(DownloadWorker.KEY_APP_NAME, appName)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            QUEUE_NAME,
            ExistingWorkPolicy.APPEND,
            request
        )

        return request.id
    }

    fun getWorkInfo(context: Context, workId: UUID): LiveData<WorkInfo> {
        return WorkManager.getInstance(context).getWorkInfoByIdLiveData(workId)
    }

    fun cancelDownload(context: Context, workId: UUID) {
        WorkManager.getInstance(context).cancelWorkById(workId)
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(QUEUE_NAME)
    }
}