package com.example.plantaura2

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.example.plantaura2.domain.usecase.GetUserEmailUseCase
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
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val sensorConnectionViewModel: SensorConnectionViewModel by viewModels()

    companion object {
        const val MY_CHANEL_ID = "myChannel"
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        createNotificationChannel()


        // Registro del ActivityResultLauncher para la cámara
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    val sensorId =
                        sensorConnectionViewModel.currentSensorId // Obtener el ID del sensor descubierto
                    if (sensorId != null) {
                        sensorConnectionViewModel.saveImage(imageBitmap, sensorId)
                    } else {
                        Log.e("MainActivity", "Sensor no encontrado")
                    }
                }
            }

        // Registro del ActivityResultLauncher para permisos
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
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
        val getUserEmailUseCase = GetUserEmailUseCase(FirebaseAuth.getInstance())

        val loginViewModelFactory = LoginViewModelFactory(authUseCase)
        val homeViewModelFactory =
            HomeViewModelFactory(getPlantNamesUseCase, getPlantIdByNameUseCase, application)
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
                ProfileViewModelFactory(deletePlantUseCase, getPlantsUseCase, changePasswordUseCase, getUserEmailUseCase)
            )[ProfileViewModel::class.java]
        val settingsViewModel: SettingsViewModel by viewModels()

        setContent {
            PlantAura2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.CAMERA
                )
            )
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
    }


    //JOSE LO CAMBIE A PUBLIC - PRIVATE DABA ERRORES Y NO  QUERIA MODIFICAR LAS OTRAS CLASS.
    public fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nombre del canal"
            val descriptionText = "Descripción del canal"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("default", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    fun createSimpleNotification(context: Context) {
        val channelId = "MY_CHANNEL_ID"
        val notificationId = 1

        // Crear el canal de notificación para Android O y versiones superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val descriptionText = "This is a notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Registrar el canal en el sistema
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)


        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Construir la notificación
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.plantaura_logo_fondo_removebg)
            .setContentTitle("PlantAura")
            .setContentText("Notificaciones de nuestras gráficas de viaje")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Notificaciones de nuestras gráficas de viaje. Haz clic para ver más detalles."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Comprobar el permiso y mostrar la notificación
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
            } else {
                // Solicitar permiso si no está concedido
                ActivityCompat.requestPermissions(
                    (context as Activity),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    notificationId
                )
            }
        }
    }


}

