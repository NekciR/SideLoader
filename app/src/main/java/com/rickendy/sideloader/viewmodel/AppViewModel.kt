package com.rickendy.sideloader.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.sideloader.data.model.AppInfo
import com.rickendy.sideloader.data.repository.AppRepository
import com.rickendy.sideloader.util.DownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppUiState {
    object Loading : AppUiState()
    data class Success(val apps: List<AppInfo>) : AppUiState()
    data class Error(val message: String) : AppUiState()
}

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _activeDownloads = MutableStateFlow<Map<String, UUID>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, UUID>> = _activeDownloads.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = AppUiState.Loading
            val result = AppRepository.getApps()
            _uiState.value = if (result.isSuccess) {
                AppUiState.Success(result.getOrNull()?.apps ?: emptyList())
            } else {
                AppUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun startDownload(context: Context, app: AppInfo) {
        val workId = DownloadManager.enqueueDownload(
            context = context,
            apkUrl = app.apkUrl,
            appName = app.name
        )
        _activeDownloads.value += (app.id to workId)
    }

    fun clearDownload(appId: String) {
        _activeDownloads.value -= appId
    }

    fun cancelDownload(context: Context, appId: String) {
        _activeDownloads.value[appId]?.let {
            DownloadManager.cancelDownload(context, it)
        }
        clearDownload(appId)
    }
}