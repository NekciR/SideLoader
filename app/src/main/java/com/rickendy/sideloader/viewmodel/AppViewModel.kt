package com.rickendy.sideloader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.sideloader.data.model.AppInfo
import com.rickendy.sideloader.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppUiState {
    object Loading : AppUiState()
    data class Success(val apps: List<AppInfo>) : AppUiState()
    data class Error(val message: String) : AppUiState()
}

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

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
}