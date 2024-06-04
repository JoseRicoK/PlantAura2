package com.example.plantaura2.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class ChangePasswordUseCase(private val auth: FirebaseAuth) {
    suspend fun changePassword(newPassword: String): Result<Unit> {
        val user = auth.currentUser
        return if (user != null) {
            try {
                user.updatePassword(newPassword).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No user is signed in"))
        }
    }
}
