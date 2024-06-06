package com.example.plantaura2.ui.settings.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.plantaura2.MainActivity
import com.example.plantaura2.domain.model.Plant
import com.example.plantaura2.domain.usecase.NotificationWorker
import com.example.plantaura2.ui.home.ui.BottomNavigationBar
import com.example.plantaura2.ui.theme.PlantAura2Theme
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(), navController: NavController) {
    val plants by viewModel.plants.collectAsState()
    var isTravelModeActive by remember { mutableStateOf(false) }
    var selectedTravelDays by remember { mutableStateOf("1 día de viaje") }
    var selectedNotificationInterval by remember { mutableStateOf("Cada 1 minuto") }
    var notificationSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Estado del Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onSettingsClick = { viewModel.onSettingsClick(navController) },
                onHomeClick = { viewModel.onHomeClick(navController) },
                onProfileClick = { viewModel.onProfileClick(navController) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)  // Add additional padding if needed
        ) {
            Text(
                text = "Ajustes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isTravelModeActive = !isTravelModeActive },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTravelModeActive) Color(0xFF4CAF50) else Color(0xFFD0BCFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(text = "Activar Modo Viaje", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (isTravelModeActive) {
                Spacer(modifier = Modifier.height(16.dp))
                DropDownMenu(
                    items = listOf(
                        "1 día de viaje", "2 días de viaje", "3 días de viaje",
                        "4 días de viaje", "5 días de viaje"
                    ),
                    label = "¿Cuántos días te vas de viaje?",
                    selectedItem = selectedTravelDays,
                    onItemSelected = { selectedTravelDays = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                DropDownMenu(
                    items = listOf(
                        "Cada 1 minuto", "Cada 2 horas", "Cada 3 horas", "Cada 4 horas",
                        "Cada 5 horas", "Cada 6 horas", "Cada 7 horas", "Cada 8 horas",
                        "Cada 9 horas", "Cada 10 horas", "Cada 12 horas", "Cada 24 horas"
                    ),
                    label = "¿Cada cuánto tiempo quieres recibir una notificación?",
                    selectedItem = selectedNotificationInterval,
                    onItemSelected = { selectedNotificationInterval = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        (context as? MainActivity)?.createSimpleNotification(context)
                        val diasViajeSeleccionados = selectedTravelDays.toIntOrNull() ?: 0
                        val intervaloNotificacionSeleccionado = selectedNotificationInterval
                        val intervalo = calcularIntervalo(diasViajeSeleccionados, intervaloNotificacionSeleccionado)

                        // Programar la notificación con el intervalo calculado
                        scheduleNotification(intervalo, context)

                        notificationSent = true
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Notificación programada",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (notificationSent) Color(0xFF4CAF50) else Color(0xFFD0BCFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(text = "Confirmar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider()
            Text(
                text = "Tus Plantas:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            PlantList(plants = plants, onDeletePlant = { plantId ->
                viewModel.onDeletePlantSelected(plantId)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Planta eliminada",
                        duration = SnackbarDuration.Short
                    )
                }
            })
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenu(items: List<String>, label: String, selectedItem: String, onItemSelected: (String) -> Unit) {
    var selectedText by remember { mutableStateOf(items[0]) }
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = label, modifier = Modifier.padding(top = 8.dp))
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
                items.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            selectedText = items[index]
                            isExpanded = false
                            onItemSelected(selectedText)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

fun calcularIntervalo(diasViajeSeleccionados: Int, intervaloNotificacionSeleccionado: String): Long {
    return when (intervaloNotificacionSeleccionado) {
        "Cada 1 minuto" -> TimeUnit.SECONDS.toMillis(1)
        "Cada 2 horas" -> TimeUnit.HOURS.toMillis(2)
        "Cada 3 horas" -> TimeUnit.HOURS.toMillis(3)
        "Cada 4 horas" -> TimeUnit.HOURS.toMillis(4)
        "Cada 5 horas" -> TimeUnit.HOURS.toMillis(5)
        "Cada 6 horas" -> TimeUnit.HOURS.toMillis(6)
        "Cada 7 horas" -> TimeUnit.HOURS.toMillis(7)
        "Cada 8 horas" -> TimeUnit.HOURS.toMillis(8)
        "Cada 9 horas" -> TimeUnit.HOURS.toMillis(9)
        "Cada 10 horas" -> TimeUnit.HOURS.toMillis(10)
        "Cada 12 horas" -> TimeUnit.HOURS.toMillis(12)
        "Cada 24 horas" -> TimeUnit.HOURS.toMillis(24)
        else -> throw IllegalArgumentException("Intervalo de notificación no válido")
    }
}

private fun scheduleNotification(intervalo: Long, context: Context) {
    val workManager = WorkManager.getInstance(context)
    val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
        intervalo, TimeUnit.MILLISECONDS
    ).build()
    workManager.enqueueUniquePeriodicWork(
        "NotificationWork",
        ExistingPeriodicWorkPolicy.REPLACE,
        notificationWorkRequest
    )
}
@Composable
fun PlantList(plants: List<Plant>, onDeletePlant: (String) -> Unit) {
    LazyColumn {
        items(plants) { plant ->
            PlantItem(plant = plant, onDeletePlant = { onDeletePlant(plant.id) })
        }
    }
}

@Composable
fun PlantItem(plant: Plant, onDeletePlant: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = plant.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onDeletePlant,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Borrar")
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    PlantAura2Theme {
        val navController = rememberNavController()
        SettingsScreen(navController = navController)
    }
}