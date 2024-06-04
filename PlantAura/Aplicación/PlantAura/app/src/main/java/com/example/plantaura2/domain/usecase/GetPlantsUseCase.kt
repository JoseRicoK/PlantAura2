package com.example.plantaura2.domain.usecase

import com.example.plantaura2.domain.model.Plant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GetPlantsUseCase(private val firestore: FirebaseFirestore) {
    suspend fun getPlants(): Result<List<Plant>> {
        return try {
            val snapshot = firestore.collection("Plantas").get().await()
            val plants = snapshot.documents.mapNotNull { document ->
                document.toObject(Plant::class.java)?.copy(id = document.id)
            }
            Result.success(plants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
