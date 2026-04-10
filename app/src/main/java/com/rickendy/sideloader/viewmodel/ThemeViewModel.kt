package com.rickendy.sideloader.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.sideloader.data.local.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _theme = MutableStateFlow("system")
    val theme: StateFlow<String> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            UserPreferences.getTheme(context).collect {
                _theme.value = it
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            UserPreferences.saveTheme(context, theme)
            _theme.value = theme
        }
    }
}