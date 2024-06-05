package com.example.plantaura2.domain.usecase


import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class GetUserEmailUseCase(private val auth: FirebaseAuth) {
    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }
}