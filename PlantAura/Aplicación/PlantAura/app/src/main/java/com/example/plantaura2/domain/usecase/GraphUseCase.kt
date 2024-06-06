package com.example.plantaura2.domain.usecase

import android.util.Log
import com.example.plantaura2.domain.model.MeasurementData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GraphUseCase(private val firestore: FirebaseFirestore) {

    companion object {
        const val TAG = "GraphUseCase"
    }

    suspend fun getMeasurementData(plantId: String): List<MeasurementData> {
        Log.d(TAG, "Fetching measurement data for plantId: $plantId")
        return try {
            val snapshot = firestore.collection("Plantas").document(plantId).get().await()
            val sensorType = snapshot.getString("sensorType")

            val dataSnapshot = firestore.collection("Plantas").document(plantId).collection("datos")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(20).get().await()
            Log.d(TAG, "Snapshot size: ${dataSnapshot.size()}")

            dataSnapshot.documents.forEach { document ->
                Log.d(TAG, "Document ID: ${document.id}")
                Log.d(TAG, "Document data: ${document.data}")
            }

            val data = dataSnapshot.documents.mapNotNull { document ->
                val timestampString = document.getString("timestamp")
                val humedadAmbiente = document.getLong("humedadAmbiente")?.toInt()
                val humedadSuelo = document.getLong("humedadSuelo")?.toInt()
                val temperatura = document.getDouble("temperatura")?.toFloat()
                val luminosidad = document.getDouble("luminosidad")?.toFloat()

                if (sensorType == "Sensor PlantAura Pro") {
                    // Datos adicionales para el sensor Pro
                    val conductividad = document.getLong("conductividad")?.toInt()
                    val ph = document.getDouble("ph")?.toFloat()
                    val nitrogeno = document.getLong("nitrogeno")?.toInt()
                    val fosforo = document.getLong("fosforo")?.toInt()
                    val potasio = document.getLong("potasio")?.toInt()
                    val salinidad = document.getLong("salinidad")?.toInt()
                    val tds = document.getLong("tds")?.toInt()

                    if (timestampString != null && humedadAmbiente != null && humedadSuelo != null && temperatura != null && luminosidad != null) {
                        MeasurementData(
                            timestampString, humedadAmbiente, humedadSuelo, temperatura, luminosidad,
                            conductividad, ph, nitrogeno, fosforo, potasio, salinidad, tds
                        )
                    } else {
                        Log.d(TAG, "Skipping document with incomplete data: ${document.data}")
                        null
                    }
                } else {
                    if (timestampString != null && humedadAmbiente != null && humedadSuelo != null && temperatura != null && luminosidad != null) {
                        MeasurementData(timestampString, humedadAmbiente, humedadSuelo, temperatura, luminosidad)
                    } else {
                        Log.d(TAG, "Skipping document with incomplete data: ${document.data}")
                        null
                    }
                }
            }
            Log.d(TAG, "Measurement data fetched: $data")
            data
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching measurement data: ${e.message}", e)
            emptyList()
        }
    }
}
