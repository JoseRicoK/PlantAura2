package com.example.plantaura2.ui.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

class ProfileViewModel : ViewModel() {
    fun onSettingsClick(navController: NavController) {
        navController.navigate("settings")
    }
    fun onHomeClick(navController: NavController) {
        navController.navigate("home")
    }
    fun onProfileClick(navController: NavController) {
        // Actualmente en la pantalla de perfil, no hacer nada
    }
}
