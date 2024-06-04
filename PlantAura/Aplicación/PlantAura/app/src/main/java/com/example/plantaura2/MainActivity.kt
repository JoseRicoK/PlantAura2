package com.example.plantaura2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.plantaura2.domain.usecase.AuthUseCase
import com.example.plantaura2.domain.usecase.GetPlantIdByNameUseCase
import com.example.plantaura2.domain.usecase.GetPlantNamesUseCase
import com.example.plantaura2.domain.usecase.SignInUseCase
import com.example.plantaura2.domain.usecase.SignUpUseCase
import com.example.plantaura2.domain.usecase.DeletePlantUseCase
import com.example.plantaura2.domain.usecase.GetPlantsUseCase
import com.example.plantaura2.domain.usecase.ChangePasswordUseCase
import com.example.plantaura2.navigation.AppNavigation
import com.example.plantaura2.ui.home.ui.HomeViewModel
import com.example.plantaura2.ui.home.ui.HomeViewModelFactory
import com.example.plantaura2.ui.login.ui.LoginViewModel
import com.example.plantaura2.ui.login.ui.LoginViewModelFactory
import com.example.plantaura2.ui.profile.ui.ProfileViewModel
import com.example.plantaura2.ui.profile.ui.ProfileViewModelFactory
import com.example.plantaura2.ui.questionHub.ui.QuestionHubViewModel
import com.example.plantaura2.ui.sensorConnection.ui.SensorConnectionViewModel
import com.example.plantaura2.ui.sensorConnection.ui.SensorConnectionViewModelFactory
import com.example.plantaura2.ui.settings.ui.SettingsViewModel
import com.example.plantaura2.ui.signup.ui.SignUpViewModel
import com.example.plantaura2.ui.signup.ui.SignUpViewModelFactory
import com.example.plantaura2.ui.theme.PlantAura2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val signUpUseCase = SignUpUseCase(FirebaseAuth.getInstance())
        val changePasswordUseCase = ChangePasswordUseCase(FirebaseAuth.getInstance())
        val getPlantNamesUseCase = GetPlantNamesUseCase(FirebaseFirestore.getInstance())
        val getPlantIdByNameUseCase = GetPlantIdByNameUseCase(FirebaseFirestore.getInstance())
        val deletePlantUseCase = DeletePlantUseCase(FirebaseFirestore.getInstance())
        val getPlantsUseCase = GetPlantsUseCase(FirebaseFirestore.getInstance())

        val loginViewModelFactory = LoginViewModelFactory(authUseCase)
        val homeViewModelFactory = HomeViewModelFactory(getPlantNamesUseCase, getPlantIdByNameUseCase)
        val signUpViewModelFactory = SignUpViewModelFactory(signUpUseCase)
        val sensorConnectionViewModelFactory = SensorConnectionViewModelFactory(this.application)

        val loginViewModel: LoginViewModel by viewModels { loginViewModelFactory }
        val signUpViewModel: SignUpViewModel by viewModels { signUpViewModelFactory }
        val homeViewModel: HomeViewModel by viewModels { homeViewModelFactory }
        val hubViewModel: QuestionHubViewModel by viewModels()
        val sensorConnectionViewModel: SensorConnectionViewModel by viewModels { sensorConnectionViewModelFactory }
        val profileViewModel =
            ViewModelProvider(
                this,
                ProfileViewModelFactory(deletePlantUseCase, getPlantsUseCase, changePasswordUseCase)
            )[ProfileViewModel::class.java]
        val settingsViewModel: SettingsViewModel by viewModels()

        setContent {
            PlantAura2Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(
                        loginViewModel,
                        signUpViewModel,
                        homeViewModel,
                        hubViewModel,
                        sensorConnectionViewModel,
                        profileViewModel,
                        settingsViewModel
                    )
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
