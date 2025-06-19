package com.example.petcare.presentation.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash") // For initial profile check
    object Home : Screen("home")
    object Walking : Screen("walking")
    object AiChatbot : Screen("ai_chatbot")
    object Ranking : Screen("ranking")
    object Settings : Screen("settings")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg -> append("/$arg") }
        }
    }
}