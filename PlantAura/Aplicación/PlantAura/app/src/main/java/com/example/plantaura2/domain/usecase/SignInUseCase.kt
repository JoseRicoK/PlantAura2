package com.example.plantaura2.domain.usecase

import com.google.firebase.auth.FirebaseAuth

class SignInUseCase(private val repository: FirebaseAuth) {
    operator fun invoke(email: String, password: String) = repository.signInWithEmailAndPassword(email, password)
}