package com.example.plantaura2.ui.plantdetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plantaura2.domain.model.MeasurementData
import com.example.plantaura2.domain.usecase.GraphUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.tasks.await

data class Plant(
    val id: String = "",
    val name: String = ""
)

class PlantDetailsViewModel(private val plantNameInput: String, private val graphUseCase: GraphUseCase) : ViewModel() {
    private val _plantName = MutableStateFlow("")
    val plantName: StateFlow<String> = _plantName

    private val _measurementData = MutableStateFlow<List<MeasurementData>>(emptyList())
    val measurementData: StateFlow<List<MeasurementData>> = _measurementData

    private val _lastHumidityAmbiente = MutableStateFlow<Int?>(null)
    val lastHumidityAmbiente: StateFlow<Int?> = _lastHumidityAmbiente

    private val _lastHumiditySuelo = MutableStateFlow<Int?>(null)
    val lastHumiditySuelo: StateFlow<Int?> = _lastHumiditySuelo

    private val _lastTemperature = MutableStateFlow<Float?>(null)
    val lastTemperature: StateFlow<Float?> = _lastTemperature

    init {
        fetchPlantDetails()
    }

    private fun fetchPlantDetails() {
        viewModelScope.launch {
            try {
                Log.d("PlantDetailsViewModel", "Fetching plant details for name: $plantNameInput")
                val snapshot = FirebaseFirestore.getInstance().collection("Plantas").whereEqualTo("name", plantNameInput).get().await()
                Log.d("PlantDetailsViewModel", "Snapshot size: ${snapshot.size()}")
                val plant = snapshot.documents.firstOrNull()?.toObject(Plant::class.java)
                if (plant != null) {
                    Log.d("PlantDetailsViewModel", "Plant found: $plant")
                    _plantName.value = plant.name
                    fetchMeasurementData(plant.id)
                } else {
                    Log.d("PlantDetailsViewModel", "No plant found with name: $plantNameInput")
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching plant details: ${e.message}", e)
            }
        }
    }

    private fun fetchMeasurementData(plantId: String) {
        viewModelScope.launch {
            try {
                Log.d("PlantDetailsViewModel", "Fetching measurement data for plantId: $plantId")
                val data = graphUseCase.getMeasurementData(plantId)
                _measurementData.value = data
                Log.d("PlantDetailsViewModel", "Measurement data fetched: $data")

                if (data.isNotEmpty()) {
                    _lastHumidityAmbiente.value = data.last().humedadAmbiente
                    _lastHumiditySuelo.value = data.last().humedadSuelo
                    _lastTemperature.value = data.last().temperatura
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching measurement data: ${e.message}", e)
            }
        }
    }
}

class PlantDetailsViewModelFactory(private val plantNameInput: String, private val graphUseCase: GraphUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailsViewModel(plantNameInput, graphUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
