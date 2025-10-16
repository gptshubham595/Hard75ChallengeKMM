package com.shubham.hard75kmm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.hard75kmm.data.repositories.AuthService
import com.shubham.hard75kmm.data.repositories.PlatformContext
import com.shubham.hard75kmm.ui.models.AuthUiState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val authService: AuthService) : ViewModel() {
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Initiates the sign-in process by calling the platform-specific implementation.
     */
    fun signIn(context: PlatformContext) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Call the platform-specific signIn method from the injected AuthService.
                //    This will show the native Google Sign-In UI on Android/iOS.
                val credential = authService.signIn(context)

                if (credential != null) {
                    // 2. If the platform returns a valid credential, use it to sign in to Firebase.
                    auth.signInWithCredential(credential)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    // This can happen if the user cancels the native sign-in flow.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Sign-in cancelled."
                        )
                    }
                }
            } catch (e: Exception) {
                // Catch any errors from the platform or Firebase.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sign-in failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Signs the user out from both Firebase and the platform's Google Sign-In service.
     */
    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    /**
     * Gets the currently signed-in Firebase user.
     */
    fun getCurrentUser() = authService.getCurrentUser()
}