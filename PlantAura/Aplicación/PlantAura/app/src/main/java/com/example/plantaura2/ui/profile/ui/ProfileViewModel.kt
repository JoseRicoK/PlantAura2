package com.example.plantaura2.ui.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.plantaura2.domain.usecase.DeletePlantUseCase
import com.example.plantaura2.domain.usecase.ChangePasswordUseCase
import com.example.plantaura2.domain.usecase.GetPlantsUseCase
import com.example.plantaura2.domain.model.Plant
import com.example.plantaura2.domain.usecase.GetUserEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(
    private val deletePlantUseCase: DeletePlantUseCase,
    private val getPlantsUseCase: GetPlantsUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getUserEmailUseCase: GetUserEmailUseCase
) : ViewModel() {
    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    private val _passwordChangeMessage = MutableLiveData<String?>()
    val passwordChangeMessage: LiveData<String?> = _passwordChangeMessage

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    init {
        loadPlants()
        _userEmail.value = getUserEmailUseCase.getUserEmail()
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

    fun onChangePassword(newPassword: String) {
        viewModelScope.launch {
            val result = changePasswordUseCase.changePassword(newPassword)
            result.onSuccess {
                _passwordChangeMessage.value = "Contraseña cambiada con éxito"
            }.onFailure { exception ->
                Log.d("MascotaFeliz", "signInWithEmail: ${exception.message}")
                _passwordChangeMessage.value = "Error al cambiar la contraseña"
            }
        }
    }

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

class ProfileViewModelFactory(
    private val deletePlantUseCase: DeletePlantUseCase,
    private val getPlantsUseCase: GetPlantsUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getUserEmailUseCase: GetUserEmailUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(deletePlantUseCase, getPlantsUseCase, changePasswordUseCase, getUserEmailUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
