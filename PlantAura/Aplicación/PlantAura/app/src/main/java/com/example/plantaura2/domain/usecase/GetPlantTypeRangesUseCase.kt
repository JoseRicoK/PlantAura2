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
                val humedadAmbienteMin = document.getLong("humedadAmbienteMin")?.toInt()
                val humedadAmbienteMax = document.getLong("humedadAmbienteMax")?.toInt()
                val humedadSueloMin = document.getLong("humedadSueloMin")?.toInt()
                val humedadSueloMax = document.getLong("humedadSueloMax")?.toInt()
                val temperaturaAmbienteMin = document.getDouble("temperaturaAmbienteMin")
                val temperaturaAmbienteMax = document.getDouble("temperaturaAmbienteMax")
                val luzMin = document.getLong("luzMin")?.toInt()
                val luzMax = document.getLong("luzMax")?.toInt()
                val conductividadMin = document.getLong("conductividadMin")?.toInt()
                val conductividadMax = document.getLong("conductividadMax")?.toInt()
                val phMin = document.getDouble("phMin")
                val phMax = document.getDouble("phMax")
                val nitrogenoMin = document.getLong("nitrogenoMin")?.toInt()
                val nitrogenoMax = document.getLong("nitrogenoMax")?.toInt()
                val fosforoMin = document.getLong("fosforoMin")?.toInt()
                val fosforoMax = document.getLong("fosforoMax")?.toInt()
                val potasioMin = document.getLong("potasioMin")?.toInt()
                val potasioMax = document.getLong("potasioMax")?.toInt()
                val salinidadMin = document.getLong("salinidadMin")?.toInt()
                val salinidadMax = document.getLong("salinidadMax")?.toInt()
                val tdsMin = document.getLong("tdsMin")?.toInt()
                val tdsMax = document.getLong("tdsMax")?.toInt()

                Log.d(TAG, "Plant type ranges fetched: humedadAmbienteMin=$humedadAmbienteMin, humedadAmbienteMax=$humedadAmbienteMax, humedadSueloMin=$humedadSueloMin, humedadSueloMax=$humedadSueloMax, temperaturaAmbienteMin=$temperaturaAmbienteMin, temperaturaAmbienteMax=$temperaturaAmbienteMax, luzMin=$luzMin, luzMax=$luzMax, conductividadMin=$conductividadMin, conductividadMax=$conductividadMax, phMin=$phMin, phMax=$phMax, nitrogenoMin=$nitrogenoMin, nitrogenoMax=$nitrogenoMax, fosforoMin=$fosforoMin, fosforoMax=$fosforoMax, potasioMin=$potasioMin, potasioMax=$potasioMax, salinidadMin=$salinidadMin, salinidadMax=$salinidadMax, tdsMin=$tdsMin, tdsMax=$tdsMax")

                PlantTypeRanges(
                    humedadAmbienteMin = humedadAmbienteMin,
                    humedadAmbienteMax = humedadAmbienteMax,
                    humedadSueloMin = humedadSueloMin,
                    humedadSueloMax = humedadSueloMax,
                    temperaturaAmbienteMin = temperaturaAmbienteMin,
                    temperaturaAmbienteMax = temperaturaAmbienteMax,
                    luzMin = luzMin,
                    luzMax = luzMax,
                    conductividadMin = conductividadMin,
                    conductividadMax = conductividadMax,
                    phMin = phMin,
                    phMax = phMax,
                    nitrogenoMin = nitrogenoMin,
                    nitrogenoMax = nitrogenoMax,
                    fosforoMin = fosforoMin,
                    fosforoMax = fosforoMax,
                    potasioMin = potasioMin,
                    potasioMax = potasioMax,
                    salinidadMin = salinidadMin,
                    salinidadMax = salinidadMax,
                    tdsMin = tdsMin,
                    tdsMax = tdsMax
                ).also { Log.d(TAG, "PlantTypeRanges instance created: $it") }
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
