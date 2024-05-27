package com.example.plantaura2.ui.sensorConnection.ui

import android.app.Application
import android.content.Context
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
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

data class Sensor(val id: String, val ip: String, val humedad: Int, val nombre: String = "")

class SensorConnectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _sensors = MutableStateFlow<List<Sensor>>(emptyList())
    val sensors: StateFlow<List<Sensor>> = _sensors

    private val firestore = FirebaseFirestore.getInstance()

    // Verifica si el dispositivo está conectado a WiFi
    private fun isConnectedToWiFi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    // Descubre el ESP32 utilizando mDNS
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
                        // Handle service removal if needed
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
                        val sensor = Sensor(sensorData.id, ipAddress, sensorData.humedad)
                        _sensors.value = _sensors.value + sensor
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

    fun saveSensorToFirebase(sensorId: String, name: String, onSuccess: () -> Unit) {
        val sensorData = hashMapOf(
            "id" to sensorId,
            "name" to name
            // Agrega otros campos que desees guardar
        )

        viewModelScope.launch {
            try {
                firestore.collection("Plantas").document(sensorId).set(sensorData).await()
                Log.d("SensorConnectionViewModel", "Sensor guardado en Firebase con éxito")
                onSuccess() // Navegar a la pantalla de detalles después de guardar
            } catch (e: Exception) {
                Log.e("SensorConnectionViewModel", "Error guardando el sensor: ${e.message}")
            }
        }
    }
}

// Factory para el ViewModel
class SensorConnectionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorConnectionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
