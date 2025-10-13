@file:OptIn(ExperimentalForeignApi::class)

package com.shubham.hard75kmm.data.repositories


import cocoapods.GoogleSignIn.GIDSignIn
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * The actual iOS implementation of the AuthService.
 * This class handles the Google Sign-In flow using the official GoogleSignIn SDK for iOS.
 */
actual class AuthService {
    private val auth = Firebase.auth

    /**
     * Initiates the Google Sign-In flow on iOS.
     * It presents the native Google Sign-In UI over the current top view controller.
     * @return AuthCredential on success, null on cancellation, or throws an exception on error.
     */
    actual suspend fun signIn(): AuthCredential? {
        // Find the top-most view controller to present the sign-in screen over.
        val topViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

        return suspendCancellableCoroutine { continuation ->
            GIDSignIn.sharedInstance.signInWithPresentingViewController(topViewController!!) { result, error ->
                when {
                    error != null -> {
                        continuation.resumeWithException(Exception(error.localizedDescription))
                    }

                    result == null -> {
                        continuation.resumeWithException(Exception("Google Sign-In failed: result is null."))
                    }

                    else -> {
                        val googleIdToken = result.user.idToken?.tokenString
                        if (googleIdToken != null) {
                            val credential = GoogleAuthProvider.credential(
                                idToken = googleIdToken,
                                accessToken = null
                            )
                            continuation.resume(credential)
                        } else {
                            continuation.resume(null) // User cancelled or no token
                        }
                    }
                }
            }
        }
    }

    actual suspend fun signOut() {
        GIDSignIn.sharedInstance.signOut()
        auth.signOut()
    }

    actual fun getCurrentUser() = auth.currentUser
}
