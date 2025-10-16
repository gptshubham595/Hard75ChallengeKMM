package com.shubham.hard75kmm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shubham.hard75kmm.data.repositories.getPlatformContext
import com.shubham.hard75kmm.ui.models.AuthUiState
import com.shubham.hard75kmm.ui.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The main entry point for the Login Screen.
 * It handles logic, state observation, and navigation events.
 *
 * @param onLoginSuccess A callback function to be invoked when the sign-in is successful.
 * This is triggered by the NavHost to perform the navigation.
 */
@Composable
fun LoginScreenRoot(onLoginSuccess: () -> Unit) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Get the platform-specific context (an Activity on Android) required for the sign-in UI.
    val context = getPlatformContext()

    // This side effect listens for a successful login state.
    // When `uiState.isSuccess` becomes true, it calls the `onLoginSuccess` lambda.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    // This is the pure UI component. It receives state and callbacks.
    LoginScreenContent(
        uiState = uiState,
        onSignInClick = { viewModel.signIn(context) }
    )
}

/**
 * The pure UI part of the Login Screen.
 * It is stateless and only displays the UI based on the provided `AuthUiState`.
 */
@Composable
fun LoginScreenContent(
    uiState: AuthUiState,
    onSignInClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the", style = MaterialTheme.typography.headlineSmall)
            Text("75 Hard Challenge", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onSignInClick) {
                    Text("Sign in with Google")
                }
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}