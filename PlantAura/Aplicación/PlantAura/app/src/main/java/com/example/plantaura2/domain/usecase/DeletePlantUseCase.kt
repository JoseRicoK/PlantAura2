package com.example.plantaura2.domain.usecase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DeletePlantUseCase(private val firestore: FirebaseFirestore) {
    suspend fun deletePlant(plantId: String): Result<Unit> {
        return try {
            firestore.collection("Plantas").document(plantId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}