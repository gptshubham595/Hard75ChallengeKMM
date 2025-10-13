package com.shubham.hard75kmm.data.repositories

import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * An expected interface for handling platform-specific authentication flows.
 * The actual implementation will be provided by each platform (androidMain, iosMain).
 */
expect class AuthService {
    suspend fun signIn(): AuthCredential?
    suspend fun signOut()
    fun getCurrentUser(): FirebaseUser?
}