package com.example.plantaura2.ui.plantdetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PlantDetailsViewModel(private val plantNameInput: String) : ViewModel() {
    private val _plantName = MutableStateFlow("")
    val plantName: StateFlow<String> = _plantName

    init {
        fetchPlantDetails()
    }

    private fun fetchPlantDetails() {
        viewModelScope.launch {
            try {
                val document = FirebaseFirestore.getInstance().collection("Plantas").whereEqualTo("name", plantNameInput).get().await()
                val name = document.documents.firstOrNull()?.getString("name") ?: "Unknown"
                _plantName.value = name
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class PlantDetailsViewModelFactory(private val plantNameInput: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailsViewModel(plantNameInput) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
