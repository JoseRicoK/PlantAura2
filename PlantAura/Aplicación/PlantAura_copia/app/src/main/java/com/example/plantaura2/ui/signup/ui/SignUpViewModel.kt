package com.example.plantaura2.ui.signup.ui

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
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
        val emailValid = isValidEmail(_email.value)
        val passwordValid = isValidPassword(_password.value)
        val passwordsMatch = _password.value == _confirmPassword.value
        _signUpEnable.value = emailValid && passwordValid && passwordsMatch
    }

    private fun isValidPassword(password: String?): Boolean {
        return password != null && password.length >= 6
    }

    private fun isValidEmail(email: String?): Boolean {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun onSignUpSelected() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(4000) // Simular una operación de red
            _isLoading.value = false
            _navigation.value = "hub"
        }
    }
}
