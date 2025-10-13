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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shubham.hard75kmm.ui.models.AuthUiState
import com.shubham.hard75kmm.ui.viewmodel.AuthViewModel

object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<AuthViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        // This effect listens for a successful login and navigates accordingly
        LaunchedEffect(uiState.isSuccess) {
            if (uiState.isSuccess) {
                // Replace the entire back stack with the ChallengeScreen
                navigator.replaceAll(ChallengeScreen)
            }
        }

        LoginScreenContent(
            uiState = uiState,
            onSignInClick = viewModel::signIn
        )
    }
}

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