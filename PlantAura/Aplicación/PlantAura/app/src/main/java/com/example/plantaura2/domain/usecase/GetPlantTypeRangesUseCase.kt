package com.example.plantaura2.domain.usecase

import android.util.Log
import com.example.plantaura2.domain.model.PlantTypeRanges
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GetPlantTypeRangesUseCase(private val firestore: FirebaseFirestore) {

    companion object {
        const val TAG = "GetPlantTypeRangesUseCase"
    }

    suspend fun getPlantTypeRanges(plantTypeCommonName: String): PlantTypeRanges? {
        Log.d(TAG, "Fetching plant type ranges for plantTypeCommonName: $plantTypeCommonName")
        return try {
            val snapshot = firestore.collection("TiposDePlantas").get().await()

            val document = snapshot.documents.firstOrNull { doc ->
                doc.getString("nombreCientifico") == plantTypeCommonName || doc.getString("nombreComun") == plantTypeCommonName
            }

            if (document != null) {
                Log.d(TAG, "Document ID: ${document.id}")
                val ranges = document.toObject(PlantTypeRanges::class.java)
                Log.d(TAG, "Plant type ranges fetched: $ranges")
                ranges
            } else {
                Log.d(TAG, "No document found for plantTypeCommonName: $plantTypeCommonName")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching plant type ranges: ${e.message}", e)
            null
        }
    }
}
