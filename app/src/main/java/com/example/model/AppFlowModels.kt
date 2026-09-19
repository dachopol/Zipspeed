package com.example.model

enum class AppScreen {
    SPLASH,
    LOGIN,
    MAIN
}

data class UserProfile(
    val name: String,
    val email: String,
    val provider: String, // "Google", "Facebook", "Guest"
    val isGuest: Boolean = false
)
