package com.shubham.hard75kmm.ui.viewmodel
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shubham.hard75kmm.data.repositories.AuthService
import com.shubham.hard75kmm.ui.models.AuthUiState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val authService: AuthService) : ScreenModel {
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun signIn() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Call the platform-specific signIn method from the AuthService
                val credential = authService.signIn()
                if (credential != null) {
                    // Sign in to Firebase with the credential from the platform
                    auth.signInWithCredential(credential)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    // This can happen if the user cancels the sign-in flow
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Sign-in cancelled.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sign-in failed: ${e.message}") }
            }
        }
    }

    fun signOut() {
        screenModelScope.launch {
            authService.signOut()
        }
    }

    fun getCurrentUser() = authService.getCurrentUser()
}
