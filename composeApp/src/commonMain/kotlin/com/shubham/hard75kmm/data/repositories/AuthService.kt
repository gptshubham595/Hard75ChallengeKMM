package com.shubham.hard75kmm.data.repositories

import androidx.compose.runtime.Composable
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * An expected class defining the contract for platform-specific authentication flows.
 * The actual implementation will be provided by each platform (androidMain, iosMain).
 */
expect class AuthService {
    /**
     * Initiates the platform-native sign-in UI and returns a Firebase credential on success.
     * @param context The platform-specific UI context (an Activity on Android) needed to show the sign-in screen.
     */
    suspend fun signIn(context: PlatformContext): AuthCredential?

    /**
     * Signs the user out of the platform's Google service and Firebase.
     */
    suspend fun signOut()

    /**
     * Retrieves the current Firebase user, if one is signed in.
     */
    fun getCurrentUser(): FirebaseUser?
}

expect class PlatformContext

@Composable
expect fun getPlatformContext(): PlatformContext