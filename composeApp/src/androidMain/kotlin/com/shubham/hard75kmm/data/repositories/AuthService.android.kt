package com.shubham.hard75kmm.data.repositories

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.shubham.hard75kmm.R
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth

/**
 * The actual Android implementation of the AuthService.
 * This class handles the Google Sign-In flow using CredentialManager.
 */
actual class AuthService(private val context: Context) {
    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    actual suspend fun signIn(): AuthCredential? {
        // Build the Google Sign-In option
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            // Use the server client ID from your google-services.json
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        // Build the overall credential request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Request the credential from the manager
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        // Check if the returned credential is a valid Google ID token
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            // Create a Firebase credential from the Google ID token
            return GoogleAuthProvider.credential(idToken = googleIdTokenCredential.idToken, null)
        }
        // Return null if sign-in fails or the credential type is wrong
        return null
    }

    actual suspend fun signOut() {
        auth.signOut()
        // You might want to clear credential state here as well if needed
    }

    actual fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
