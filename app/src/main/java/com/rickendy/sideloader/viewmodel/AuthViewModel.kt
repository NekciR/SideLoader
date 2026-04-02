package com.rickendy.sideloader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.sideloader.data.model.User
import com.rickendy.sideloader.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow(false)
    val loginError: StateFlow<Boolean> = _loginError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = false
            val user = AuthRepository.login(username, password)
            if (user != null) {
                _currentUser.value = user
                _loginError.value = false
            } else {
                _loginError.value = true
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = false
    }
}