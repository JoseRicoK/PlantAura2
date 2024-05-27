package com.example.plantaura2.domain.usecase

import com.example.plantaura2.domain.model.PlantType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GetPlantTypeByNameUseCase(private val firestore: FirebaseFirestore) {

    suspend fun execute(plantTypeName: String): PlantType? {
        return try {
            val snapshot = firestore.collection("TiposDePlantas")
                .whereEqualTo("nombreComun", plantTypeName)
                .get()
                .await()

            val plantType = snapshot.documents.firstOrNull()?.toObject(PlantType::class.java)
            plantType
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
