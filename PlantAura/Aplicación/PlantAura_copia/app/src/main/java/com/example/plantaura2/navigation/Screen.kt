package com.example.plantaura2.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signUp")
    object Home : Screen("home")
    object Hub : Screen("hub")
    object Profile : Screen("profile")
    object SensorConnection : Screen("sensorConnection")
    // Add other screens here
}