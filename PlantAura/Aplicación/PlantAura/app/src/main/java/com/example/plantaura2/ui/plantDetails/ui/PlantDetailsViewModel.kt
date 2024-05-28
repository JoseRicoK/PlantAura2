package com.example.plantaura2.ui.plantdetails.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plantaura2.data.repository.Recommendations
import com.example.plantaura2.domain.model.MeasurementData
import com.example.plantaura2.domain.model.PlantTypeRanges
import com.example.plantaura2.domain.usecase.GetPlantTypeByNameUseCase
import com.example.plantaura2.domain.usecase.GetPlantTypeRangesUseCase
import com.example.plantaura2.domain.usecase.GraphUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Plant(
    val id: String = "",
    val name: String = "",
    val plantType: String = ""
)

class PlantDetailsViewModel(
    private val plantNameInput: String,
    private val graphUseCase: GraphUseCase,
    private val getPlantTypeByNameUseCase: GetPlantTypeByNameUseCase,
    private val getPlantTypeRangesUseCase: GetPlantTypeRangesUseCase
) : ViewModel() {
    private val _plantName = MutableStateFlow("")
    val plantName: StateFlow<String> = _plantName

    private val _plantType = MutableStateFlow("")
    val plantType: StateFlow<String> = _plantType

    private val _measurementData = MutableStateFlow<List<MeasurementData>>(emptyList())
    val measurementData: StateFlow<List<MeasurementData>> = _measurementData

    private val _lastHumidityAmbiente = MutableStateFlow<Int?>(null)
    val lastHumidityAmbiente: StateFlow<Int?> = _lastHumidityAmbiente

    private val _lastHumiditySuelo = MutableStateFlow<Int?>(null)
    val lastHumiditySuelo: StateFlow<Int?> = _lastHumiditySuelo

    private val _lastTemperature = MutableStateFlow<Float?>(null)
    val lastTemperature: StateFlow<Float?> = _lastTemperature

    private val _lastLuminosidad = MutableStateFlow<Float?>(null)
    val lastLuminosidad: StateFlow<Float?> = _lastLuminosidad

    private val _plantTypeRanges = MutableStateFlow<PlantTypeRanges?>(null)
    val plantTypeRanges: StateFlow<PlantTypeRanges?> = _plantTypeRanges

    private val _revive = MutableStateFlow(false)
    val revive: StateFlow<Boolean> = _revive

    private val _plantId = MutableStateFlow<String?>(null)
    val plantId: StateFlow<String?> = _plantId

    init {
        fetchPlantDetails()
    }

    private fun fetchPlantDetails() {
        viewModelScope.launch {
            try {
                Log.d("PlantDetailsViewModel", "Fetching plant details for name: $plantNameInput")
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("Plantas")
                    .whereEqualTo("name", plantNameInput)
                    .get()
                    .await()

                Log.d("PlantDetailsViewModel", "Snapshot size: ${snapshot.size()}")
                val plant = snapshot.documents.firstOrNull()?.toObject(Plant::class.java)
                if (plant != null) {
                    Log.d("PlantDetailsViewModel", "Plant found: $plant")
                    _plantName.value = plant.name
                    _plantType.value = plant.plantType
                    _plantId.value = plant.id  // Set plantId here
                    fetchMeasurementData(plant.id)
                    fetchPlantTypeRanges(plant.plantType)
                } else {
                    Log.d("PlantDetailsViewModel", "No plant found with name: $plantNameInput")
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching plant details: ${e.message}", e)
            }
        }
    }

    fun toggleRevive(plantId: String, currentReviveState: Boolean) {
        viewModelScope.launch {
            try {
                val newReviveState = !currentReviveState
                FirebaseFirestore.getInstance()
                    .collection("Plantas")
                    .document(plantId)
                    .update("revive", newReviveState)
                    .await()
                _revive.value = newReviveState
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error toggling revive: ${e.message}", e)
            }
        }
    }

    private fun fetchMeasurementData(plantId: String) {
        viewModelScope.launch {
            try {
                Log.d("PlantDetailsViewModel", "Fetching measurement data for plantId: $plantId")
                val data = graphUseCase.getMeasurementData(plantId)
                Log.d("PlantDetailsViewModel", "Original measurement data: $data")
                _measurementData.value = data

                if (data.isNotEmpty()) {
                    val reversedData = data.asReversed()
                    Log.d("PlantDetailsViewModel", "Reversed measurement data: $reversedData")
                    _lastHumidityAmbiente.value = reversedData.last().humedadAmbiente
                    _lastHumiditySuelo.value = reversedData.last().humedadSuelo
                    _lastTemperature.value = reversedData.last().temperatura
                    _lastLuminosidad.value = reversedData.last().luminosidad
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching measurement data: ${e.message}", e)
            }
        }
    }

    private fun fetchPlantTypeRanges(plantType: String) {
        viewModelScope.launch {
            try {
                Log.d("PlantDetailsViewModel", "Fetching plant type ranges for plantType: $plantType")
                val ranges = getPlantTypeRangesUseCase.getPlantTypeRanges(plantType)
                if (ranges != null) {
                    _plantTypeRanges.value = ranges
                    Log.d("PlantDetailsViewModel", "Plant type ranges fetched: $ranges")
                } else {
                    Log.d("PlantDetailsViewModel", "No ranges found for plantType: $plantType")
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching plant type ranges: ${e.message}", e)
            }
        }
    }

    fun getRecommendation(parameterName: String, value: Float, range: ClosedFloatingPointRange<Float>): String? {
        val recommendation = Recommendations.recommendations.find { it.parameterName == parameterName }
        return if (recommendation != null) {
            when {
                value > range.endInclusive -> recommendation.highMessage
                value < range.start -> recommendation.lowMessage
                else -> null
            }
        } else {
            null
        }
    }
}



class PlantDetailsViewModelFactory(
    private val plantNameInput: String,
    private val graphUseCase: GraphUseCase,
    private val getPlantTypeByNameUseCase: GetPlantTypeByNameUseCase,
    private val getPlantTypeRangesUseCase: GetPlantTypeRangesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailsViewModel(
                plantNameInput,
                graphUseCase,
                getPlantTypeByNameUseCase,
                getPlantTypeRangesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

