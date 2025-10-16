package com.shubham.hard75kmm.data.repositories

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
actual typealias PlatformContext = Activity

// The constructor is now empty! No more context is needed here.
actual class AuthService {
    private val auth = Firebase.auth

    // The function now receives the Activity context from the UI
    actual suspend fun signIn(context: PlatformContext): AuthCredential? {
        // Create the CredentialManager here, using the correct Activity context.
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            // This now correctly and safely uses the Activity context to find the string resource.
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return GoogleAuthProvider.credential(idToken = googleIdTokenCredential.idToken, null)
        }
        return null
    }

    actual suspend fun signOut() {
        auth.signOut()
    }

    actual fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}

@Composable
actual fun getPlatformContext(): PlatformContext {
    // The context provided by LocalContext in a Composable inside an Activity
    // is always an instance of that Activity.
    return LocalActivity.current as Activity
}