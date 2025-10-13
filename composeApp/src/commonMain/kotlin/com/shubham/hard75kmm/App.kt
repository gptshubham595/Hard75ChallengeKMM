package com.shubham.hard75kmm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.shubham.hard75kmm.data.repositories.AuthService
import com.shubham.hard75kmm.ui.screens.ChallengeScreen
import com.shubham.hard75kmm.ui.screens.LoginScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import hard75kmm.composeapp.generated.resources.Res
import hard75kmm.composeapp.generated.resources.compose_multiplatform
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val authService: AuthService = koinInject()

        // Determine the starting screen based on whether a user is already logged in.
        val startScreen = if (authService.getCurrentUser() != null) {
            ChallengeScreen
        } else {
            LoginScreen
        }

        MaterialTheme {
            // The Navigator is the core of Voyager. It manages the screen back stack.
            // We initialize it with our calculated startScreen.
            Navigator(screen = startScreen) { navigator ->
                // Apply a slide transition for all screen changes
                SlideTransition(navigator)
            }
        }
    }
}