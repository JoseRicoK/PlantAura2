package com.example.plantaura2.domain.usecase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GetPlantIdByNameUseCase(private val firestore: FirebaseFirestore) {
    suspend fun getPlantIdByName(plantName: String): String? {
        val snapshot = firestore.collection("Plantas")
            .whereEqualTo("name", plantName)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.id
    }
}