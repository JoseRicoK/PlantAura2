package com.example.plantaura2.ui.plantDetails.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plantaura2.data.GoogleCloudAuthHelper
import com.example.plantaura2.data.repository.Recommendations
import com.example.plantaura2.domain.model.MeasurementData
import com.example.plantaura2.domain.model.Plant
import com.example.plantaura2.domain.model.PlantTypeRanges
import com.example.plantaura2.domain.usecase.GetPlantTypeByNameUseCase
import com.example.plantaura2.domain.usecase.GetPlantTypeRangesUseCase
import com.example.plantaura2.domain.usecase.GraphUseCase
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.roundToInt


class PlantDetailsViewModel(
    private val plantNameInput: String,
    private val graphUseCase: GraphUseCase,
    private val getPlantTypeByNameUseCase: GetPlantTypeByNameUseCase,
    private val getPlantTypeRangesUseCase: GetPlantTypeRangesUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val _plantName = MutableStateFlow("")
    val plantName: StateFlow<String> = _plantName

    private val _plantType = MutableStateFlow("")
    val plantType: StateFlow<String> = _plantType

    private val _sensorType = MutableStateFlow("")
    val sensorType: StateFlow<String> = _sensorType

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

    private val _lastConductividad = MutableStateFlow<Int?>(null)
    val lastConductividad: StateFlow<Int?> = _lastConductividad

    private val _lastPh = MutableStateFlow<Float?>(null)
    val lastPh: StateFlow<Float?> = _lastPh

    private val _lastNitrogeno = MutableStateFlow<Int?>(null)
    val lastNitrogeno: StateFlow<Int?> = _lastNitrogeno

    private val _lastFosforo = MutableStateFlow<Int?>(null)
    val lastFosforo: StateFlow<Int?> = _lastFosforo

    private val _lastPotasio = MutableStateFlow<Int?>(null)
    val lastPotasio: StateFlow<Int?> = _lastPotasio

    private val _lastSalinidad = MutableStateFlow<Int?>(null)
    val lastSalinidad: StateFlow<Int?> = _lastSalinidad

    private val _lastTds = MutableStateFlow<Int?>(null)
    val lastTds: StateFlow<Int?> = _lastTds

    private val _plantTypeRanges = MutableStateFlow<PlantTypeRanges?>(null)
    val plantTypeRanges: StateFlow<PlantTypeRanges?> = _plantTypeRanges

    private val _revive = MutableStateFlow(false)
    val revive: StateFlow<Boolean> = _revive

    private val _plantId = MutableStateFlow<String?>(null)
    val plantId: StateFlow<String?> = _plantId

    private val _recommendations = MutableStateFlow<List<String>>(emptyList())
    val recommendations: StateFlow<List<String>> = _recommendations

    private val _hiddenRecommendations = MutableStateFlow<Set<Int>>(emptySet())
    val hiddenRecommendations: StateFlow<Set<Int>> = _hiddenRecommendations

    private val _filteredRecommendations = MutableStateFlow<List<String>>(emptyList())
    val filteredRecommendations: StateFlow<List<String>> = _filteredRecommendations

    val saludPredicha = MutableLiveData<Int>()

    private val client = OkHttpClient()

    private var scaler: MinMaxScaler? = null

    init {
        fetchPlantDetails()
        loadScaler()
    }





    // Prediccion de la salud de la planta
    private fun loadScaler() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val assetManager = getApplication<Application>().assets
                val inputStream = assetManager.open("scaler.json")
                val jsonString = inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonObject = JSONObject(jsonString)

                scaler = MinMaxScaler(
                    jsonObject.getJSONArray("scale_").toDoubleArray(),
                    jsonObject.getJSONArray("min_").toDoubleArray(),
                    jsonObject.getJSONArray("data_min_").toDoubleArray(),
                    jsonObject.getJSONArray("data_max_").toDoubleArray(),
                    jsonObject.getJSONArray("data_range_").toDoubleArray()
                )

                Log.d("PlantDetailsViewModel", "Scaler loaded successfully")
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error loading scaler: ${e.message}", e)
            }
        }
    }

    private fun JSONArray.toDoubleArray(): DoubleArray {
        val array = DoubleArray(this.length())
        for (i in 0 until this.length()) {
            array[i] = this.getDouble(i)
        }
        return array
    }

    fun predictSalud() {
        viewModelScope.launch {
            try {
                val datosRecientes = arrayOf(
                    floatArrayOf(30.0f, 70.0f, 25.0f, 200.0f),
                    floatArrayOf(29.0f, 58.0f, 24.5f, 195.0f),
                    floatArrayOf(28.0f, 65.0f, 24.0f, 190.0f),
                    floatArrayOf(27.0f, 63.0f, 23.5f, 185.0f),
                    floatArrayOf(26.0f, 50.0f, 23.0f, 180.0f)
                )

                val scaledData = scaler?.transform(datosRecientes) ?: datosRecientes
                val response = predictSaludOnCloud(scaledData)
                val predictedSalud = response?.getJSONArray("predictions")?.getJSONArray(0)?.getDouble(0)
                predictedSalud?.let {
                    saludPredicha.postValue(it.toInt())
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error during prediction: ${e.message}", e)
            }
        }
    }

    private suspend fun predictSaludOnCloud(scaledData: Array<FloatArray>): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                val projectID = "48209340015"
                val endpointID = "4046402901331935232"
                val endpointUrl = "https://us-central1-aiplatform.googleapis.com/v1/projects/$projectID/locations/us-central1/endpoints/$endpointID:predict"

                val instances = JSONArray().apply {
                    put(JSONObject().apply {
                        put("lstm_input", JSONArray(scaledData.map { row -> JSONArray(row.toList()) }))
                    })
                }

                val requestBody = JSONObject().apply {
                    put("instances", instances)
                }.toString()

                val token = GoogleCloudAuthHelper(getApplication<Application>()).fetchAuthToken()
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(endpointUrl)
                    .post(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), requestBody))
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("PlantDetailsViewModel", "Prediction request failed: ${response.body?.string()}")
                    return@withContext null
                }

                val responseBody = response.body?.string()
                responseBody?.let {
                    return@withContext JSONObject(it)
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error during cloud prediction: ${e.message}", e)
                null
            }
        }
    }



    // Recomendaciones
    private fun fetchRecommendations(plantId: String) {
        viewModelScope.launch {
            try {
                val documentSnapshot = FirebaseFirestore.getInstance()
                    .collection("Plantas")
                    .document(plantId)
                    .get()
                    .await()

                val plant = documentSnapshot.toObject(Plant::class.java)
                if (plant != null) {
                    _recommendations.value = plant.recommendations
                    _hiddenRecommendations.value = plant.hiddenRecommendations.toSet()
                    updateFilteredRecommendations()

                    Log.d("PlantDetailsViewModel", "Fetched recommendations: ${plant.recommendations}")
                    Log.d("PlantDetailsViewModel", "Fetched hidden recommendations: ${plant.hiddenRecommendations}")
                } else {
                    Log.d("PlantDetailsViewModel", "No plant found with id: $plantId")
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching recommendations: ${e.message}", e)
            }
        }
    }

    private fun updateFilteredRecommendations() {
        val hidden = _hiddenRecommendations.value
        _filteredRecommendations.value = _recommendations.value.filterIndexed { index, _ -> !hidden.contains(index) }
    }

    fun hideRecommendation(index: Int) {
        viewModelScope.launch {
            val currentHiddenRecommendations = _hiddenRecommendations.value.toMutableSet()
            currentHiddenRecommendations.add(index)
            _hiddenRecommendations.value = currentHiddenRecommendations
            plantId.value?.let {
                FirebaseFirestore.getInstance()
                    .collection("Plantas")
                    .document(it)
                    .update("hiddenRecommendations", currentHiddenRecommendations.toList())
            }
            updateFilteredRecommendations()
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



    // Detalles de la planta
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
                    _sensorType.value = plant.sensorType
                    _plantId.value = plant.id // Guardar el plantId
                    fetchMeasurementData(plant.id)
                    fetchPlantTypeRanges(plant.plantType)
                    fetchRecommendations(plant.id)
                    fetchReviveState(plant.id)
                } else {
                    Log.d("PlantDetailsViewModel", "No plant found with name: $plantNameInput")
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching plant details: ${e.message}", e)
            }
        }
    }

    private fun fetchReviveState(plantId: String) {
        viewModelScope.launch {
            try {
                val documentSnapshot = FirebaseFirestore.getInstance()
                    .collection("Plantas")
                    .document(plantId)
                    .get()
                    .await()

                val reviveState = documentSnapshot.getBoolean("revive") ?: false
                _revive.value = reviveState
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error fetching revive state: ${e.message}", e)
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

                if (!newReviveState) {
                    // Si se desactiva el modo revive, limpiar las recomendaciones ocultas
                    _hiddenRecommendations.value = emptySet()
                    FirebaseFirestore.getInstance()
                        .collection("Plantas")
                        .document(plantId)
                        .update("hiddenRecommendations", emptyList<Int>())
                        .await()
                }
            } catch (e: Exception) {
                Log.e("PlantDetailsViewModel", "Error toggling revive: ${e.message}", e)
            }
        }
    }

    private fun fetchMeasurementData(plantId: String) {
        viewModelScope.launch {
            try {
                val data = graphUseCase.getMeasurementData(plantId)
                Log.d("PlantDetailsViewModel", "Fetched measurement data: $data")
                _measurementData.value = data

                if (data.isNotEmpty()) {
                    val reversedData = data.asReversed()
                    _lastHumidityAmbiente.value = reversedData.last().humedadAmbiente
                    _lastHumiditySuelo.value = reversedData.last().humedadSuelo
                    _lastTemperature.value = reversedData.last().temperatura
                    _lastLuminosidad.value = reversedData.last().luminosidad
                    _lastConductividad.value = reversedData.last().conductividad
                    _lastPh.value = reversedData.last().ph
                    _lastNitrogeno.value = reversedData.last().nitrogeno
                    _lastFosforo.value = reversedData.last().fosforo
                    _lastPotasio.value = reversedData.last().potasio
                    _lastSalinidad.value = reversedData.last().salinidad
                    _lastTds.value = reversedData.last().tds

                    Log.d("PlantDetailsViewModel", "Last values updated: HumAmb=${_lastHumidityAmbiente.value}, HumSuel=${_lastHumiditySuelo.value}, Temp=${_lastTemperature.value}, Lum=${_lastLuminosidad.value}, Cond=${_lastConductividad.value}, pH=${_lastPh.value}, N=${_lastNitrogeno.value}, P=${_lastFosforo.value}, K=${_lastPotasio.value}, Sal=${_lastSalinidad.value}, TDS=${_lastTds.value}")
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

}



class PlantDetailsViewModelFactory(
    private val plantNameInput: String,
    private val graphUseCase: GraphUseCase,
    private val getPlantTypeByNameUseCase: GetPlantTypeByNameUseCase,
    private val getPlantTypeRangesUseCase: GetPlantTypeRangesUseCase,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailsViewModel(
                plantNameInput,
                graphUseCase,
                getPlantTypeByNameUseCase,
                getPlantTypeRangesUseCase,
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// Clase para escalar los datos de entrada
data class MinMaxScaler(
    val scale: DoubleArray,
    val min: DoubleArray,
    val dataMin: DoubleArray,
    val dataMax: DoubleArray,
    val dataRange: DoubleArray
) {
    fun transform(data: Array<FloatArray>): Array<FloatArray> {
        return data.map { row ->
            row.mapIndexed { index, value ->
                ((value - dataMin[index]) / dataRange[index]).toFloat()
            }.toFloatArray()
        }.toTypedArray()
    }
}


