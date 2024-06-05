package com.example.plantaura2.ui.settings.ui


import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.plantaura2.MainActivity
import com.example.plantaura2.domain.usecase.NotificationWorker
import com.example.plantaura2.ui.home.ui.BottomNavigationBar
import com.example.plantaura2.ui.theme.PlantAura2Theme
import java.util.concurrent.TimeUnit
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(), navController: NavController) {
    var isTravelModeActive by remember { mutableStateOf(false) }
    var selectedTravelDays by remember { mutableStateOf("") }
    var selectedNotificationInterval by remember { mutableStateOf("") }

    var notificationSent by remember { mutableStateOf(false) }


    val context = LocalContext.current




    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "Settings Screen",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {isTravelModeActive = !isTravelModeActive},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTravelModeActive) Color(0xFF4CAF50) else Color(0xFFD0BCFF)
                )) {
                Text(text = "Modo viaje")
            }

            if (isTravelModeActive) {
                Spacer(modifier = Modifier.height(16.dp))
                dropDownMenu1 { selected ->
                    selectedTravelDays = selected
                }
                Spacer(modifier = Modifier.height(16.dp))

                dropDownMenu2 { selected ->
                    selectedNotificationInterval = selected
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {(context as? MainActivity)?.createSimpleNotification(context)
                    val diasViajeSeleccionados = selectedTravelDays?.toIntOrNull() ?: 0
                    val intervaloNotificacionSeleccionado = selectedNotificationInterval ?: ""
                    val intervalo = calcularIntervalo(diasViajeSeleccionados, intervaloNotificacionSeleccionado)

                    // Programar la notificación con el intervalo calculado
                    scheduleNotification(intervalo, context)

                    notificationSent = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (notificationSent) Color(0xFF4CAF50) else Color(0xFFD0BCFF))
            ) {
                Text(text = "Confirmar y probar notificación")
            }
        }

        if (notificationSent) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "La notificación se ha enviado", color = Color.Green)

        }


    }

    BottomNavigationBar(
        navController = navController,
        onSettingsClick = { viewModel.onSettingsClick(navController) },
        onHomeClick = { viewModel.onHomeClick(navController) },
        onProfileClick = { viewModel.onProfileClick(navController) }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dropDownMenu1(onSelectionChanged: (String) -> Unit) {
    //Lista de dias de viaje en los que el usuario elige
    val list1 = listOf(
        "1 día de viaje", "2 días de viaje", "3 días de viaje",
        "4 días de viaje", "5 días de viaje"
    )

    var selectedText by remember { mutableStateOf(list1[0]) }
    var isExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }) {
                list1.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            selectedText = list1[index]
                            isExpanded = false
                            onSelectionChanged(selectedText)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Text(text = "Días de viaje seleccionados... $selectedText")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dropDownMenu2(onSelectionChanged: (String) -> Unit) {
    //Lista de cada cuando recibe la notificacion el usuario
    val list2 = listOf(
        "Cada 1 minuto","Cada 2 horas", "Cada 3 horas", "Cada 4 horas", "Cada 5 horas", "Cada 6 horas",
        "Cada 7 horas", "Cada 8 horas", "Cada 9 horas", "Cada 10 horas", "Cada 12 horas",
        "Cada 24 horas"
    )

    var selectedText by remember { mutableStateOf(list2[0]) }
    var isExpanded by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }) {
                list2.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            selectedText = list2[index]
                            isExpanded = false
                            onSelectionChanged(selectedText)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Text(text = "Horas de notificaciones seleccionadas... $selectedText")
    }
}




fun calcularIntervalo(
    diasViajeSeleccionados: Int,
    intervaloNotificacionSeleccionado: String
): Long {
    return when (intervaloNotificacionSeleccionado) {
        "Cada 1 minuto" -> TimeUnit.MINUTES.toMillis(1)
        "Cada 2 horas" -> 2 * TimeUnit.HOURS.toMinutes(1)
        "Cada 3 horas" -> 3 * TimeUnit.HOURS.toMinutes(1)
        "Cada 4 horas" -> 4 * TimeUnit.HOURS.toMinutes(1)
        "Cada 5 horas" -> 5 * TimeUnit.HOURS.toMinutes(1)
        "Cada 6 horas" -> 6 * TimeUnit.HOURS.toMinutes(1)
        "Cada 7 horas" -> 7 * TimeUnit.HOURS.toMinutes(1)
        "Cada 8 horas" -> 8 * TimeUnit.HOURS.toMinutes(1)
        "Cada 9 horas" -> 9 * TimeUnit.HOURS.toMinutes(1)
        "Cada 10 horas" -> 10 * TimeUnit.HOURS.toMinutes(1)
        "Cada 12 horas" -> 12 * TimeUnit.HOURS.toMinutes(1)
        "Cada 24 horas" -> TimeUnit.DAYS.toMinutes(1)
        else -> throw IllegalArgumentException("Intervalo de notificación no válidoo")

    }

}

private fun scheduleNotification(intervalo: Long, context: Context) {
    val workManager = WorkManager.getInstance(context)
    val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
        repeatInterval = intervalo,
        repeatIntervalTimeUnit = TimeUnit.MILLISECONDS
    ).build()
    workManager.enqueueUniquePeriodicWork(
        "NotificationWork",
        ExistingPeriodicWorkPolicy.REPLACE,
        notificationWorkRequest
    )
}

@Preview
@Composable
fun HubScreenPreview() {
    PlantAura2Theme {
        val navController = rememberNavController()
        SettingsScreen(navController = navController)
    }


}