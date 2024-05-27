package com.example.plantaura2.domain.usecase

import com.example.plantaura2.domain.model.MeasurementData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class GraphUseCase(private val firestore: FirebaseFirestore) {

    companion object {
        const val TAG = "GraphUseCase"
    }

    suspend fun getMeasurementData(plantId: String): List<MeasurementData> {
        Log.d(TAG, "Fetching measurement data for plantId: $plantId")
        return try {
            val snapshot = firestore.collection("Plantas").document(plantId).collection("datos")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(20).get().await()
            Log.d(TAG, "Snapshot size: ${snapshot.size()}")

            snapshot.documents.forEach { document ->
                Log.d(TAG, "Document ID: ${document.id}")
                Log.d(TAG, "Document data: ${document.data}")
            }

            val data = snapshot.documents.mapNotNull { document ->
                val timestampString = document.getString("timestamp")
                val humedadAmbiente = document.getLong("humedadAmbiente")?.toInt()
                val humedadSuelo = document.getLong("humedadSuelo")?.toInt()
                val temperatura = document.getDouble("temperatura")?.toFloat()

                Log.d(TAG, "Parsed data - timestamp: $timestampString, humedadAmbiente: $humedadAmbiente, humedadSuelo: $humedadSuelo, temperatura: $temperatura")

                if (timestampString != null && humedadAmbiente != null && humedadSuelo != null && temperatura != null) {
                    MeasurementData(timestampString, humedadAmbiente, humedadSuelo, temperatura)
                } else {
                    Log.d(TAG, "Skipping document with incomplete data: ${document.data}")
                    null
                }
            }
            Log.d(TAG, "Measurement data fetched: $data")
            data
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching measurement data: ${e.message}", e)
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
