package com.example.plantaura2.domain.usecase

import com.example.plantaura2.domain.model.HumidityData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class GraphUseCase(private val firestore: FirebaseFirestore) {

    companion object {
        const val TAG = "GraphUseCase"
    }

    suspend fun getHumidityData(plantId: String): List<HumidityData> {
        return try {
            val snapshot = firestore.collection("Plantas").document(plantId).collection("datos")
                .orderBy("timestamp").limit(20).get().await()
            val data = snapshot.documents.mapNotNull { document ->
                val timestampString = document.getString("timestamp") ?: return@mapNotNull null
                val timestamp = convertTimestamp(timestampString) ?: return@mapNotNull null
                val humidity = document.getLong("humedad")?.toInt() ?: return@mapNotNull null
                HumidityData(timestampString, humidity)
            }
            Log.d(TAG, "Humidity data fetched: $data")
            data
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching humidity data: ${e.message}", e)
            emptyList()
        }
    }

    private fun convertTimestamp(timestampString: String): Long? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.parse(timestampString)?.time
        } catch (e: Exception) {
            Log.e(TAG, "Error converting timestamp: ${e.message}", e)
            null
        }
    }
}
