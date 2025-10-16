@file:OptIn(ExperimentalForeignApi::class)

package com.shubham.hard75kmm.data.repositories

import androidx.compose.runtime.Composable
import cocoapods.GoogleSignIn.GIDSignIn
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * The actual iOS implementation of the AuthService.
 * This class handles the Google Sign-In flow using the official GoogleSignIn SDK for iOS.
 */
@OptIn(ExperimentalForeignApi::class)
actual class AuthService {
    private val auth = Firebase.auth

    /**
     * Initiates the Google Sign-In flow on iOS.
     * It presents the native Google Sign-In UI over the current top view controller.
     * @return AuthCredential on success, null on cancellation, or throws an exception on error.
     */
    actual suspend fun signIn(context: PlatformContext): AuthCredential? {
        // Find the top-most view controller to present the sign-in screen over.
        val topViewController = findTopViewController()

        // We use suspendCancellableCoroutine to bridge the callback-based
        // iOS SDK with Kotlin's suspend functions.
        return suspendCancellableCoroutine { continuation ->
            if (topViewController == null) {
                continuation.resumeWithException(Exception("Could not find top view controller."))
                return@suspendCancellableCoroutine
            }

            GIDSignIn.sharedInstance.signInWithPresentingViewController(topViewController) { result, error ->
                when {
                    // If there's an error from the SDK, resume the coroutine with an exception.
                    error != null -> {
                        continuation.resumeWithException(Exception(error.localizedDescription))
                    }
                    // If the result is null (shouldn't happen without an error), fail gracefully.
                    result == null -> {
                        continuation.resumeWithException(Exception("Google Sign-In failed: result is null."))
                    }
                    // On success, extract the ID token.
                    else -> {
                        val googleIdToken = result.user.idToken?.tokenString
                        if (googleIdToken != null) {
                            // Create a Firebase credential from the token.
                            val credential = GoogleAuthProvider.credential(
                                idToken = googleIdToken,
                                accessToken = null
                            )
                            // Resume the coroutine successfully with the credential.
                            continuation.resume(credential)
                        } else {
                            // If there's no token (e.g., user cancelled), resume with null.
                            continuation.resume(null)
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

    actual fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Helper function to find the top-most view controller in the hierarchy.
     */
    private fun findTopViewController(): UIViewController? {
        var viewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (viewController?.presentedViewController != null) {
            viewController = viewController.presentedViewController
        }
        return viewController
    }
}

actual class PlatformContext

@Composable
actual fun getPlatformContext(): PlatformContext {
    return PlatformContext()
}