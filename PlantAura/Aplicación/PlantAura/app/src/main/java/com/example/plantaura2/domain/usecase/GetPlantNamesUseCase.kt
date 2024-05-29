package com.example.plantaura2.domain.usecase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GetPlantNamesUseCase(private val firestore: FirebaseFirestore) {
    suspend fun getPlantNames(): Result<List<String>> {
        return try {
            val snapshot = firestore.collection("Plantas").get().await()
            val plantNames = snapshot.documents.mapNotNull { it.getString("name") }
            Result.success(plantNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
