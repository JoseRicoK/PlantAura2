package com.example.plantaura2.ui.signup.ui

import android.util.Patterns
import androidx.lifecycle.*
import com.example.plantaura2.domain.usecase.SignUpUseCase
import kotlinx.coroutines.launch

class SignUpViewModel(private val signUpUseCase: SignUpUseCase) : ViewModel() {
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String> = _confirmPassword

    private val _signUpEnable = MutableLiveData<Boolean>()
    val signUpEnable: LiveData<Boolean> = _signUpEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _navigation = MutableLiveData<String?>()
    val navigation: LiveData<String?> = _navigation

    fun onEmailChanged(email: String) {
        _email.value = email
        validateForm()
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        validateForm()
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        validateForm()
    }

    private fun validateForm() {
        val emailValid = isValidEmail(_email.value ?: "")
        val passwordValid = isValidPassword(_password.value ?: "")
        val passwordsMatch = _password.value == _confirmPassword.value
        _signUpEnable.value = emailValid && passwordValid && passwordsMatch
    }

    private fun isValidPassword(password: String): Boolean = password.length >= 6

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun onNavigationHandled() {
        _navigation.value = null
    }

    fun onSignUpSelected() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = signUpUseCase.signUpWithEmail(_email.value!!, _password.value!!)
            _isLoading.value = false
            if (result.isSuccess) {
                _navigation.value = "home"
            } else {
                _navigation.value = "error" // Manejar error de registro
            }
        }
    }
}

class SignUpViewModelFactory(private val signUpUseCase: SignUpUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(signUpUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}