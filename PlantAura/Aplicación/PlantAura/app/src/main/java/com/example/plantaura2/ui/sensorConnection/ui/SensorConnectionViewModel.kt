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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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

class SensorConnectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _message = MutableStateFlow("Buscando ESP32...")
    val message: StateFlow<String> = _message

    // Actualiza el mensaje y registra en el log
    fun updateMessage(newMessage: String) {
        _message.value = newMessage
        Log.d("SensorConnectionViewModel", "Message updated: $newMessage")
    }

    // Verifica si el dispositivo está conectado a WiFi
    private fun isConnectedToWiFi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        Log.d("SensorConnectionViewModel", "isConnectedToWiFi")
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    // Descubre el ESP32 utilizando mDNS
    fun discoverESP32(context: Context) {
        if (!isConnectedToWiFi(context)) {
            updateMessage("Error: El dispositivo no está conectado a WiFi")
            Log.e("SensorConnectionViewModel", "El dispositivo no está conectado a WiFi")
            return
        } else {
            Log.d("SensorConnectionViewModel", "El dispositivo está conectado a WiFi")
        }

        enableMulticast()

        viewModelScope.launch {
            try {
                Log.d("SensorConnectionViewModel", "Starting mDNS discovery")
                val jmdns = withContext(Dispatchers.IO) {
                    JmDNS.create(InetAddress.getByName("192.168.5.222")).apply {
                        Log.d("SensorConnectionViewModel", "JmDNS created")
                    }
                }

                jmdns.addServiceListener("_http._tcp.local.", object : ServiceListener {
                    override fun serviceAdded(event: ServiceEvent) {
                        Log.d("SensorConnectionViewModel", "Service added: " + event.info)
                        // Forzar resolución del servicio
                        event.info?.let { serviceInfo ->;
                            if (serviceInfo.hasData()) {
                                Log.d("SensorConnectionViewModel", "Service already resolved: " + serviceInfo)
                                handleServiceResolved(serviceInfo)
                            } else {
                                jmdns.requestServiceInfo(event.type, event.name, true)
                            }
                        }
                    }

                    override fun serviceRemoved(event: ServiceEvent) {
                        Log.d("SensorConnectionViewModel", "Service removed: " + event.info)
                    }

                    override fun serviceResolved(event: ServiceEvent) {
                        Log.d("SensorConnectionViewModel", "Service resolved: " + event.info)
                        handleServiceResolved(event.info)
                    }
                })

                Log.d("SensorConnectionViewModel", "ServiceListener added")

                // Aumenta el tiempo de espera a 60 segundos
                delay(60000)  // Esperar 60 segundos para descubrir servicios
                Log.d("SensorConnectionViewModel", "Timeout for mDNS discovery")

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("SensorConnectionViewModel", "IOException during mDNS discovery: ${e.message}")
                updateMessage("Error: ${e.message}")
            }
        }
    }

    fun enableMulticast() {
        val wifiManager = getApplication<Application>().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifiManager.createMulticastLock("myMulticastLock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()
        Log.d("SensorConnectionViewModel", "Multicast Lock acquired")
    }


    // Maneja la resolución del servicio
    private fun handleServiceResolved(serviceInfo: ServiceInfo) {
        val esp32Address = serviceInfo.inet4Addresses.firstOrNull()?.hostAddress
        if (esp32Address != null) {
            Log.d("SensorConnectionViewModel", "ESP32 Address: $esp32Address")
            fetchMessageFromESP32(esp32Address)
        } else {
            Log.e("SensorConnectionViewModel", "Error: Dirección IP del ESP32 no encontrada")
            updateMessage("Error: Dirección IP del ESP32 no encontrada")
        }
    }

    // Definir una interfaz para Retrofit
    interface ApiService {
        @GET("data")
        suspend fun getData(): Response<String>
    }

    // Configurar Retrofit en el ViewModel
    private fun fetchMessageFromESP32(ipAddress: String) {
        viewModelScope.launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://$ipAddress/") // Retrofit necesita un esquema
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            try {
                val response = withContext(Dispatchers.IO) {
                    service.getData()
                }
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("SensorConnectionViewModel", "HTTP response successful: $responseData")
                    updateMessage(responseData ?: "Sin datos")
                } else {
                    Log.e("SensorConnectionViewModel", "Error en la respuesta: ${response.code()}")
                    updateMessage("Error en la respuesta: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SensorConnectionViewModel", "Exception during HTTP request: ${e.message}")
                updateMessage("Error: ${e.message}")
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
