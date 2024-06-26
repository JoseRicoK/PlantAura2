package com.example.plantaura2.ui.sensorConnection.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

data class Sensor(val id: String, val sensor: String)

class SensorConnectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _sensors = MutableStateFlow<List<Sensor>>(emptyList())
    val sensors: StateFlow<List<Sensor>> = _sensors

    private val firestore = FirebaseFirestore.getInstance()

    private val _plantTypes = MutableStateFlow<List<String>>(emptyList())
    val plantTypes: StateFlow<List<String>> = _plantTypes

    var currentSensorId: String? = null

    init {
        fetchPlantTypes()
    }

    fun saveImage(imageBitmap: Bitmap, sensorId: String) {
        val sensor = _sensors.value.find { it.id == sensorId }
        if (sensor == null) {
            Log.e("saveImage", "Sensor with ID $sensorId not found")
            return
        }

        val storageDir = File(getApplication<Application>().filesDir, "com.example.plantaura2.data.imagesPlants")
        Log.d("saveImage", "Storage directory: ${storageDir.absolutePath}")

        if (!storageDir.exists()) {
            val dirCreated = storageDir.mkdir()
            Log.d("saveImage", "Storage directory created: $dirCreated")
        } else {
            Log.d("saveImage", "Storage directory already exists")
        }

        val imageFile = File(storageDir, "sensor_${sensor.id}.jpg")
        Log.d("saveImage", "Image file path: ${imageFile.absolutePath}")

        try {
            val fos = FileOutputStream(imageFile)
            Log.d("saveImage", "FileOutputStream created")

            val compressed = imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            Log.d("saveImage", "Image compressed: $compressed")

            fos.close()
            Log.d("saveImage", "FileOutputStream closed")

            // Listar archivos en el directorio
            val files = storageDir.listFiles()
            if (files != null) {
                for (file in files) {
                    Log.d("saveImage", "File in directory: ${file.name}")
                }
            } else {
                Log.d("saveImage", "No files found in directory")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("saveImage", "IOException: ${e.message}")
        }
    }

    private fun isConnectedToWiFi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun discoverESP32(context: Context) {
        if (!isConnectedToWiFi(context)) {
            Log.e("SensorConnectionViewModel", "El dispositivo no está conectado a WiFi")
            return
        }

        enableMulticast()

        viewModelScope.launch {
            try {
                val jmdns = withContext(Dispatchers.IO) {
                    JmDNS.create(InetAddress.getByName("192.168.50.102")).apply {
                        Log.d("SensorConnectionViewModel", "JmDNS created")
                    }
                }

                jmdns.addServiceListener("_http._tcp.local.", object : ServiceListener {
                    override fun serviceAdded(event: ServiceEvent) {
                        event.info?.let { serviceInfo ->
                            if (serviceInfo.hasData()) {
                                handleServiceResolved(serviceInfo)
                            } else {
                                jmdns.requestServiceInfo(event.type, event.name, true)
                            }
                        }
                    }

                    override fun serviceRemoved(event: ServiceEvent) {

                    }

                    override fun serviceResolved(event: ServiceEvent) {
                        handleServiceResolved(event.info)
                    }
                })

                delay(10000)  // Esperar 10 segundos para descubrir servicios

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("SensorConnectionViewModel", "IOException during mDNS discovery: ${e.message}")
            }
        }
    }

    private fun enableMulticast() {
        val wifiManager = getApplication<Application>().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifiManager.createMulticastLock("myMulticastLock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()
        Log.d("SensorConnectionViewModel", "Multicast Lock acquired")
    }

    private fun handleServiceResolved(serviceInfo: ServiceInfo) {
        val esp32Address = serviceInfo.inet4Addresses.firstOrNull()?.hostAddress
        if (esp32Address != null) {
            fetchMessageFromESP32(esp32Address)
        } else {
            Log.e("SensorConnectionViewModel", "Error: Dirección IP del ESP32 no encontrada")
        }
    }

    interface ApiService {
        @GET("data")
        suspend fun getData(): Response<Sensor>
    }


    private fun fetchMessageFromESP32(ipAddress: String) {
        viewModelScope.launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://$ipAddress/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            try {
                val response = withContext(Dispatchers.IO) {
                    service.getData()
                }
                if (response.isSuccessful) {
                    val sensorData = response.body()
                    if (sensorData != null) {
                        // Crear el sensor usando el id y el sensor recibidos
                        val sensor = Sensor(sensorData.id, sensorData.sensor)
                        _sensors.value = _sensors.value + sensor

                        currentSensorId = sensor.id
                        Log.d("SensorConnectionViewModel", "Current Sensor ID: $currentSensorId")
                    }
                } else {
                    Log.e("SensorConnectionViewModel", "Error en la respuesta: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SensorConnectionViewModel", "Exception during HTTP request: ${e.message}")
            }
        }
    }

    private fun fetchPlantTypes() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("TiposDePlantas").get().await()
                val types = snapshot.documents.map { it.getString("nombreComun") ?: "" }
                _plantTypes.value = types
            } catch (e: Exception) {
                Log.e("PlantTypeViewModel", "Error fetching plant types: ${e.message}")
            }
        }
    }

    fun saveSensorToFirebase(sensorId: String, name: String, plantType: String, onSuccess: () -> Unit) {
        val sensor = _sensors.value.find { it.id == sensorId }
        if (sensor == null) {
            Log.e("saveSensorToFirebase", "Sensor with ID $sensorId not found")
            return
        }
        val sensorData = hashMapOf(
            "id" to sensorId,
            "name" to name,
            "plantType" to plantType,
            "sensorType" to sensor.sensor,
            "revive" to false,
            "recommendations" to listOf(
                "Elimina todos los tallos y hojas marchitas.",
                "Retira, con sumo cuidado, la parte superficial del sustrato.",
                "Saca el cepellón de la planta y ponlo en agua tibia 10 minutos.",
                "Sécalo un poco y ponlo en tierra nueva.",
                "Coloca la planta en un sitio iluminado."
            )
        )

        viewModelScope.launch {
            try {
                firestore.collection("Plantas").document(sensorId).set(sensorData).await()
                Log.d("SensorConnectionViewModel", "Sensor guardado en Firebase con éxito")
                onSuccess()
            } catch (e: Exception) {
                Log.e("SensorConnectionViewModel", "Error guardando el sensor: ${e.message}")
            }
        }
    }

}

class SensorConnectionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorConnectionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}