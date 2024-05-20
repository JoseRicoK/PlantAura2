package com.example.plantaura2.ui.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.plantaura2.domain.usecase.GetPlantNamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val getPlantNamesUseCase: GetPlantNamesUseCase) : ViewModel() {
    private val _plantNames = MutableStateFlow<List<String>>(emptyList())
    val plantNames: StateFlow<List<String>> = _plantNames

    init {
        loadPlantNames()
    }

    private fun loadPlantNames() {
        viewModelScope.launch {
            val result = getPlantNamesUseCase.getPlantNames()
            result.onSuccess { plantNamesList ->
                _plantNames.value = plantNamesList
                Log.d("HomeViewModel", "Nombres de plantas cargados: $plantNamesList")
            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error loading plant names", exception)
            }
        }
    }

    fun onPlusSelected(navController: NavController) {
        navController.navigate("sensorConnection")
    }

    fun onSettingsClick(navController: NavController) {
        navController.navigate("settings")
    }

    fun onHomeClick(navController: NavController) {
        navController.navigate("home")
    }

    fun onProfileClick(navController: NavController) {
        navController.navigate("profile")
    }

    fun onPlantSelected(navController: NavController, plantName: String) {
        navController.navigate("plantDetails/$plantName")
    }
}

class HomeViewModelFactory(private val getPlantNamesUseCase: GetPlantNamesUseCase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(getPlantNamesUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
