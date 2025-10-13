package com.shubham.hard75kmm.ui.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)