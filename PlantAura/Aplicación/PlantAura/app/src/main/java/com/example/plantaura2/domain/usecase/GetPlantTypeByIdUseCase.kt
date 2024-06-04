package com.example.plantaura2.domain.usecase

import com.example.plantaura2.domain.model.PlantType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GetPlantTypeByIdUseCase(private val firestore: FirebaseFirestore) {
    suspend fun execute(plantTypeId: String): PlantType? {
        val snapshot = firestore.collection("TiposDePlantas").document(plantTypeId).get().await()
        return snapshot.toObject(PlantType::class.java)
    }
}