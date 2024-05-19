package com.example.plantaura2.ui.plantDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plantaura2.ui.sensorConnection.ui.Sensor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlantDetailsViewModel(private val sensorId: String) : ViewModel() {

    private val _sensorData = MutableStateFlow<Sensor?>(null)
    val sensorData: StateFlow<Sensor?> = _sensorData

    init {
        loadSensorData()
    }

    private fun loadSensorData() {
        // Simulate data loading, you can replace this with actual data fetching logic
        viewModelScope.launch {
            // Suppose we fetch the data from Firebase or any other source
            val sensor = Sensor(sensorId, "192.168.5.222", 50) // Example data
            _sensorData.value = sensor
        }
    }

    fun refreshData() {
        // Refresh data every 60 seconds
        viewModelScope.launch {
            while (true) {
                delay(60000)
                loadSensorData()
            }
        }
    }
}

class PlantDetailsViewModelFactory(private val sensorId: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailsViewModel(sensorId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
