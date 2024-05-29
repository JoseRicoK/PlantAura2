package com.example.plantaura2.ui.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.plantaura2.domain.usecase.GetPlantIdByNameUseCase
import com.example.plantaura2.domain.usecase.GetPlantNamesUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Plant(
    val name: String,
    val revive: Boolean
)

class HomeViewModel(
    private val getPlantNamesUseCase: GetPlantNamesUseCase,
    private val getPlantIdByNameUseCase: GetPlantIdByNameUseCase
) : ViewModel() {
    private val _plantNames = MutableStateFlow<List<Plant>>(emptyList())
    val plantNames: StateFlow<List<Plant>> = _plantNames

    init {
        loadPlantNames()
    }

    fun loadPlantNames() {
        viewModelScope.launch {
            val result = getPlantNamesUseCase.getPlantNames()
            result.onSuccess { plantNamesList ->
                val plants = plantNamesList.map { plantName ->
                    val plantId = getPlantIdByNameUseCase.getPlantIdByName(plantName)
                    val revive = plantId?.let { getReviveState(it) } ?: false
                    Plant(name = plantName, revive = revive)
                }
                _plantNames.value = plants
                Log.d("HomeViewModel", "Nombres de plantas cargados: $plants")
            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error loading plant names", exception)
            }
        }
    }

    private suspend fun getReviveState(plantId: String): Boolean {
        val documentSnapshot = FirebaseFirestore.getInstance()
            .collection("Plantas")
            .document(plantId)
            .get()
            .await()

        return documentSnapshot.getBoolean("revive") ?: false
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

class HomeViewModelFactory(
    private val getPlantNamesUseCase: GetPlantNamesUseCase,
    private val getPlantIdByNameUseCase: GetPlantIdByNameUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(getPlantNamesUseCase, getPlantIdByNameUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
