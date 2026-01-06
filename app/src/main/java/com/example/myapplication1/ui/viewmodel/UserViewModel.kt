package com.example.myapplication1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.data.local.entity.User
import com.example.myapplication1.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for authentication
 */
data class AuthState(
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI State for registration
 */
data class RegisterState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

/**
 * ViewModel for User authentication and management
 */
class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Authentication state
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Registration state
    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()
    
    /**
     * Register a new user
     */
    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = _registerState.value.copy(isLoading = true, error = null, success = false)
            
            val result = userRepository.registerUser(username, email, password)
            result.fold(
                onSuccess = { user ->
                    _registerState.value = _registerState.value.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )
                    // Auto-login after registration
                    loginUser(username, password)
                },
                onFailure = { exception ->
                    _registerState.value = _registerState.value.copy(
                        isLoading = false,
                        success = false,
                        error = exception.message ?: "Registration failed"
                    )
                }
            )
        }
    }
    
    /**
     * Login user
     */
    fun loginUser(identifier: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = userRepository.loginUser(identifier, password)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        currentUser = user,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        currentUser = null,
                        isLoading = false,
                        error = exception.message ?: "Login failed"
                    )
                }
            )
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        _authState.value = AuthState(
            isAuthenticated = false,
            currentUser = null,
            isLoading = false,
            error = null
        )
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser(): User? {
        return _authState.value.currentUser
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return _authState.value.isAuthenticated
    }
    
    /**
     * Clear registration state
     */
    fun clearRegisterState() {
        _registerState.value = RegisterState()
    }
    
    /**
     * Clear auth error
     */
    fun clearAuthError() {
        _authState.value = _authState.value.copy(error = null)
    }
}

