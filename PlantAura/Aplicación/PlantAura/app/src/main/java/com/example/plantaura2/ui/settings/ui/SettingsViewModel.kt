package com.example.plantaura2.ui.settings.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.plantaura2.domain.model.Plant
import com.example.plantaura2.domain.usecase.ChangePasswordUseCase
import com.example.plantaura2.domain.usecase.DeletePlantUseCase
import com.example.plantaura2.domain.usecase.GetPlantsUseCase
import com.example.plantaura2.domain.usecase.GetUserEmailUseCase
import com.example.plantaura2.ui.profile.ui.ProfileViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val deletePlantUseCase: DeletePlantUseCase,
    private val getPlantsUseCase: GetPlantsUseCase
) : ViewModel() {
    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    fun onSettingsClick(navController: NavController) {
        // Actualmente en la pantalla de ajustes, no hacer nada
    }
    fun onHomeClick(navController: NavController) {
        navController.navigate("home")
    }
    fun onProfileClick(navController: NavController) {
        navController.navigate("profile")
    }

    init {
        loadPlants()
    }
    private fun loadPlants() {
        viewModelScope.launch {
            val result = getPlantsUseCase.getPlants()
            result.onSuccess { plantList ->
                _plants.value = plantList
                Log.d("ProfileViewModel", "Plantas cargadas: $plantList")
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error cargando las plantas", exception)
            }
        }
    }

    fun onDeletePlantSelected(plantId: String) {
        viewModelScope.launch {
            val result = deletePlantUseCase.deletePlant(plantId)
            result.onSuccess {
                Log.d("ProfileViewModel", "Planta eliminada con éxito: $plantId")
                loadPlants()
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error al eliminar la planta", exception)
            }
        }
    }
}
class SettingsViewModelFactory(
    private val deletePlantUseCase: DeletePlantUseCase,
    private val getPlantsUseCase: GetPlantsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(deletePlantUseCase, getPlantsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}