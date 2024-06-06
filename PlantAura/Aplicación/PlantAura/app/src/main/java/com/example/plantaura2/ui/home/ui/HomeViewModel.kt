package com.example.plantaura2.ui.home.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
import java.io.File

data class Plant(
    val name: String,
    val revive: Boolean,
    val id: String?,
    val hasImage: Boolean
)

class HomeViewModel(
    private val getPlantNamesUseCase: GetPlantNamesUseCase,
    private val getPlantIdByNameUseCase: GetPlantIdByNameUseCase,
    application: Application
) : AndroidViewModel(application) {
    private val _plantNames = MutableStateFlow<List<Plant>>(emptyList())
    val plantNames: StateFlow<List<Plant>> = _plantNames
    val imageDirectory = File(application.filesDir, "com.example.plantaura2.data.imagesPlants")

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
                    val hasImage = plantId?.let { hasImage(it) } ?: false
                    Plant(name = plantName, revive = revive, id = plantId, hasImage = hasImage)
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

    private fun hasImage(plantId: String): Boolean {
        if (!imageDirectory.exists()) {
            return false
        }
        val files = imageDirectory.listFiles() ?: return false
        return files.any { it.name.contains(plantId) }
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
    private val getPlantIdByNameUseCase: GetPlantIdByNameUseCase,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(getPlantNamesUseCase, getPlantIdByNameUseCase, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
