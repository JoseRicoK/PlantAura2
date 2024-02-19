package com.example.plantaura2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.plantaura2.domain.usecase.AuthUseCase
import com.example.plantaura2.domain.usecase.SignInUseCase
import com.example.plantaura2.navigation.AppNavigation
import com.example.plantaura2.ui.home.ui.HomeViewModel
import com.example.plantaura2.ui.home.ui.HomeViewModelFactory
import com.example.plantaura2.ui.login.ui.LoginViewModel
import com.example.plantaura2.ui.login.ui.LoginViewModelFactory
import com.example.plantaura2.ui.questionHub.ui.QuestionHubViewModel
import com.example.plantaura2.ui.signup.ui.SignUpViewModel
import com.example.plantaura2.ui.theme.PlantAura2Theme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authUseCase = AuthUseCase(FirebaseAuth.getInstance())
        val signInUseCase = SignInUseCase(FirebaseAuth.getInstance())

        val factory = LoginViewModelFactory(authUseCase)
        val homeViewModelFactory = HomeViewModelFactory(signInUseCase)

        val loginViewModel: LoginViewModel by viewModels { factory }
        val signUpViewModel: SignUpViewModel by viewModels()
        val homeViewModel: HomeViewModel by viewModels { homeViewModelFactory }
        val hubViewModel: QuestionHubViewModel by viewModels()

        setContent {
            PlantAura2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(loginViewModel, signUpViewModel, homeViewModel, hubViewModel)
                }
            }
        }
    }
}


/*
@Composable
fun AppContent(loginViewModel: LoginViewModel) {
    // Observamos el LiveData desde el ViewModel
    val currentScreen by loginViewModel.navigation.observeAsState()

    when (currentScreen) {
        "hub" -> HubScreen(
            onYesClicked = { /* acción de clic para Sí */ },
            onNoClicked = { /* acción de clic para No */ }
        )
        "signUp" -> SignUpScreen(SignUpViewModel())
        "home" -> HomeScreen()

        else -> LoginScreen(loginViewModel)
    }
}*/