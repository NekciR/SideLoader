package com.rickendy.sideloader.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.sideloader.data.local.UserPreferences
import com.rickendy.sideloader.data.model.User
import com.rickendy.sideloader.data.remote.RetrofitClient
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

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    init {
        restoreSession()
        RetrofitClient.onTokenRefreshed = { newToken ->
            _accessToken.value = newToken
            viewModelScope.launch {
                UserPreferences.saveLoggedInUser(
                    context,
                    _currentUser.value?.username ?: "",
                    _currentUser.value?.displayName ?: "",
                    newToken,
                    _refreshToken.value ?: ""
                )
            }
        }
        RetrofitClient.onSessionExpired = {
            viewModelScope.launch {
                UserPreferences.clearLoggedInUser(context)
                _currentUser.value = null
                _accessToken.value = null
                _refreshToken.value = null
            }
        }
    }



    private fun restoreSession() {
        viewModelScope.launch {
            UserPreferences.getLoggedInUser(context).collect { savedUser ->
                if (savedUser != null && _currentUser.value == null) {
                    _refreshToken.value = savedUser.refreshToken
                    val result = AuthRepository.refreshToken(savedUser.refreshToken)
                    if (result.isSuccess) {
                        result.getOrNull()?.let {
                            _currentUser.value = User(
                                username = savedUser.username,
                                displayName = savedUser.displayName
                            )
                            _accessToken.value = it.accessToken
                            _loginError.value = false
                            RetrofitClient.updateTokens(it.accessToken, savedUser.refreshToken)
                        }
                    } else {
                        UserPreferences.clearLoggedInUser(context)
                    }
                }
                kotlinx.coroutines.delay(800)
                _isSessionRestored.value = true
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = false
            val result = AuthRepository.login(username, password)
            if (result.isSuccess) {
                val response = result.getOrNull()!!
                _currentUser.value = User(
                    username = username,
                    displayName = username
                )
                _accessToken.value = response.accessToken
                _refreshToken.value = response.refreshToken
                RetrofitClient.updateTokens(response.accessToken, response.refreshToken)
                UserPreferences.saveLoggedInUser(
                    context,
                    username,
                    username,
                    response.accessToken,
                    response.refreshToken
                )
                _loginError.value = false
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
            _accessToken.value = null
            _refreshToken.value = null
            _loginError.value = false
            RetrofitClient.updateTokens(null, null)
        }
    }
}