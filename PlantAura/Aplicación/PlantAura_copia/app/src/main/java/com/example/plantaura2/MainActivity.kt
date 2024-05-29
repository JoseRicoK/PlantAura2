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
import com.example.plantaura2.domain.usecase.GetPlantNamesUseCase
import com.example.plantaura2.domain.usecase.SignInUseCase
import com.example.plantaura2.navigation.AppNavigation
import com.example.plantaura2.ui.home.ui.HomeViewModel
import com.example.plantaura2.ui.home.ui.HomeViewModelFactory
import com.example.plantaura2.ui.login.ui.LoginViewModel
import com.example.plantaura2.ui.login.ui.LoginViewModelFactory
import com.example.plantaura2.ui.questionHub.ui.QuestionHubViewModel
import com.example.plantaura2.ui.sensorConnection.ui.SensorConnectionViewModel
import com.example.plantaura2.ui.sensorConnection.ui.SensorConnectionViewModelFactory
import com.example.plantaura2.ui.signup.ui.SignUpViewModel
import com.example.plantaura2.ui.theme.PlantAura2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registro del ActivityResultLauncher para permisos
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (!granted) {
                // Algunos permisos no han sido concedidos. Aquí puedes manejar este caso.
            }
        }

        // Solicitar permisos BLE necesarios
        checkAndRequestPermissions()

        // Configuración y ViewModel inicializaciones
        val authUseCase = AuthUseCase(FirebaseAuth.getInstance())
        val signInUseCase = SignInUseCase(FirebaseAuth.getInstance())
        val getPlantNamesUseCase = GetPlantNamesUseCase(FirebaseFirestore.getInstance())
        val factory = LoginViewModelFactory(authUseCase)
        val homeViewModelFactory = HomeViewModelFactory(getPlantNamesUseCase)
        val sensorConnectionViewModelFactory = SensorConnectionViewModelFactory(this.application)

        val loginViewModel: LoginViewModel by viewModels { factory }
        val signUpViewModel: SignUpViewModel by viewModels()
        val homeViewModel: HomeViewModel by viewModels { homeViewModelFactory }
        val hubViewModel: QuestionHubViewModel by viewModels()
        val sensorConnectionViewModel: SensorConnectionViewModel by viewModels { sensorConnectionViewModelFactory }

        setContent {
            PlantAura2Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(loginViewModel, signUpViewModel, homeViewModel, hubViewModel, sensorConnectionViewModel)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED -> {
                // Solicitar los permisos
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                )
            }
            // Puedes continuar con operaciones BLE si ya tienes los permisos
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