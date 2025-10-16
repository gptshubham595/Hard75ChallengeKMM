package com.shubham.hard75kmm.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object LoginScreen : Route

    @Serializable
    data object ChallengeScreen : Route

    @Serializable
    data object EditTasksScreen : Route

    @Serializable
    data object GalleryScreen : Route

    @Serializable
    data object LeaderboardScreen : Route
}