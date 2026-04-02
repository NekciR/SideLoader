package com.rickendy.sideloader.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.sideloader.data.local.UserPreferences
import com.rickendy.sideloader.data.model.User
import com.rickendy.sideloader.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow(false)
    val loginError: StateFlow<Boolean> = _loginError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSessionRestored = MutableStateFlow(false)
    val isSessionRestored: StateFlow<Boolean> = _isSessionRestored.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            UserPreferences.getLoggedInUser(context).collect { savedUser ->
                if (savedUser != null && _currentUser.value == null) {
                    _currentUser.value = User(
                        username = savedUser.first,
                        password = "",
                        displayName = savedUser.second
                    )
                }
                kotlinx.coroutines.delay(2000)
                _isSessionRestored.value = true
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = false
            val user = AuthRepository.login(username, password)
            if (user != null) {
                _currentUser.value = user
                _loginError.value = false
                UserPreferences.saveLoggedInUser(context, user.username, user.displayName)
            } else {
                _loginError.value = true
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            UserPreferences.clearLoggedInUser(context)
            _currentUser.value = null
            _loginError.value = false
        }
    }
}