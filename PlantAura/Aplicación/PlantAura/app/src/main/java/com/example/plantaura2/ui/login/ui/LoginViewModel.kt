package com.example.plantaura2.ui.login.ui

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.plantaura2.domain.usecase.AuthUseCase
import kotlinx.coroutines.launch


class LoginViewModel(private val authUseCase: AuthUseCase) : ViewModel() {
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _loginEnable = MutableLiveData<Boolean>()
    val loginEnable: LiveData<Boolean> = _loginEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun onLoginChanged(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = isValidEmail(email) && isValidPassword(password)
    }

    private fun isValidPassword(password: String): Boolean = password.length >= 6

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun onLoginSelected(email: String, password: String, navController: NavController) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authUseCase.signInWithEmail(email, password)
            _isLoading.value = false
            result.onSuccess {
                navigateToHome(navController)

            }.onFailure { exception ->
                Log.d("MascotaFeliz", "signInWithEmail: ${exception.message}")
            }
        }
    }

    fun onSignUpSelected(navController: NavController) {
        navigateToSignUp(navController)
    }




    fun navigateToHome(navController: NavController) {
        navController.navigate("home")
    }

    fun navigateToSignUp(navController: NavController) {
        navController.navigate("signUp")
    }
}


class LoginViewModelFactory(private val authUseCase: AuthUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ViewModel for the login screen

class LoginViewModel: ViewModel(){
private val auth: FirebaseAuth = Firebase.auth
private val _loading = MutableLiveData(false)

fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit)
= viewModelScope.launch{
try {
auth.signInWithEmailAndPassword(email, password)
.addOnCompleteListener { task->
if (task.isSuccessful){
Log.d("MascotaFeliz", "signInWithEmail logueado")
home()
}
else{
Log.d("MascotaFeliz", "signInWithEmail: ${task.result.toString()}")
}
}
} catch (ex: Exception){
Log.d("MascotaFeliz", "signInWithEmail: ${ex.message}")
}
}

}*/