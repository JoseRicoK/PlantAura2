package com.example.plantaura2.ui.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

class SettingsViewModel : ViewModel() {
    fun onSettingsClick(navController: NavController) {
        // Actualmente en la pantalla de ajustes, no hacer nada
    }
    fun onHomeClick(navController: NavController) {
        navController.navigate("home")
    }
    fun onProfileClick(navController: NavController) {
        navController.navigate("profile")
    }
}


