package com.example.plantaura2.ui.plantdetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plantaura2.domain.model.HumidityData
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

    private val _humidityData = MutableStateFlow<List<HumidityData>>(emptyList())
    val humidityData: StateFlow<List<HumidityData>> = _humidityData

    private val _lastHumidity = MutableStateFlow<Int?>(null)
    val lastHumidity: StateFlow<Int?> = _lastHumidity

    private val _humidityDeviation = MutableStateFlow<Float?>(null)
    val humidityDeviation: StateFlow<Float?> = _humidityDeviation

    init {
        fetchPlantDetails()
    }

    private fun fetchPlantDetails() {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance().collection("Plantas").whereEqualTo("name", plantNameInput).get().await()
                val plant = snapshot.documents.firstOrNull()?.toObject(Plant::class.java)
                if (plant != null) {
                    _plantName.value = plant.name
                    fetchHumidityData(plant.id)
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching plant details: ${e.message}", e)
            }
        }
    }

    private fun fetchHumidityData(plantId: String) {
        viewModelScope.launch {
            try {
                val data = graphUseCase.getHumidityData(plantId)
                _humidityData.value = data
                Log.d("PlantDetailsViewModel", "Humidity data fetched: $data")

                if (data.isNotEmpty()) {
                    _lastHumidity.value = data.last().humidity
                    val last10Values = data.takeLast(10).map { it.humidity }
                    val average = last10Values.average().toFloat()
                    _humidityDeviation.value = ((last10Values.last() - average) / average) * 100
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching humidity data: ${e.message}", e)
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
