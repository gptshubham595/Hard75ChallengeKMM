package com.shubham.hard75kmm.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shubham.hard75kmm.data.repositories.AuthService
import com.shubham.hard75kmm.ui.screens.ChallengeScreenRoot
import com.shubham.hard75kmm.ui.screens.EditTasksScreenRoot
import com.shubham.hard75kmm.ui.screens.GalleryScreenRoot
import com.shubham.hard75kmm.ui.screens.LeaderboardScreenRoot
import com.shubham.hard75kmm.ui.screens.LoginScreenRoot
import org.koin.compose.koinInject

@Composable
fun GetNavGraph(navController: NavHostController) {
    val authService: AuthService = koinInject()

    // Determine the starting screen based on whether a user is already logged in.
    val startDestination: Route = if (authService.getCurrentUser() != null) {
        Route.ChallengeScreen
    } else {
        Route.LoginScreen
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Route.LoginScreen> {
            MaterialTheme {
                LoginScreenRoot(
                    onLoginSuccess = {
                        navController.navigate(Route.ChallengeScreen) {
                            popUpTo(Route.LoginScreen) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable<Route.ChallengeScreen> {
            MaterialTheme {
                ChallengeScreenRoot(navController = navController)
            }
        }

        composable<Route.EditTasksScreen> {
            MaterialTheme {
                EditTasksScreenRoot(navController = navController)
            }
        }

        composable<Route.GalleryScreen> {
            MaterialTheme {
                GalleryScreenRoot(navController = navController)
            }
        }

        composable<Route.LeaderboardScreen> {
            MaterialTheme {
                LeaderboardScreenRoot(navController = navController)
            }
        }
    }
}