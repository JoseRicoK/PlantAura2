package com.example.plantaura2.ui.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plantaura2.domain.usecase.SignInUseCase
import kotlinx.coroutines.launch

class HomeViewModel(private val signInUseCase: SignInUseCase) : ViewModel() {
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun signInWithEmailAndPassword(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                signInUseCase(email, password).addOnCompleteListener { task ->
                    _loading.value = false
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        task.exception?.message?.let { onError(it) }
                    }
                }
            } catch (ex: Exception) {
                _loading.value = false
                onError(ex.message ?: "An error occurred")
            }
        }
    }
}

class HomeViewModelFactory(private val signInUseCase: SignInUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(signInUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
