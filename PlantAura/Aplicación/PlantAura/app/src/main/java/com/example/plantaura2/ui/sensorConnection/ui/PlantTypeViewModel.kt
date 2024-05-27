package com.example.plantaura2.ui.sensorConnection.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PlantTypeViewModel(application: Application) : AndroidViewModel(application) {
    private val _plantTypes = MutableStateFlow<List<String>>(emptyList())
    val plantTypes: StateFlow<List<String>> = _plantTypes

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchPlantTypes()
    }

    private fun fetchPlantTypes() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("TiposDePlantas").get().await()
                val types = snapshot.documents.mapNotNull { it.getString("nombreComun") }
                _plantTypes.value = types
            } catch (e: Exception) {
                Log.e("PlantTypeViewModel", "Error fetching plant types: ${e.message}")
            }
        }
    }
}
