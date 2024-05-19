package com.example.plantaura2.ui.sensorConnection.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog

@Composable
fun SensorConnectionScreen(viewModel: SensorConnectionViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current
    val sensors by viewModel.sensors.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedSensorId by remember { mutableStateOf<String?>(null) }

    if (showDialog) {
        SensorNameDialog(
            onDismiss = { showDialog = false },
            onSave = { name ->
                selectedSensorId?.let { sensorId ->
                    viewModel.saveSensorToFirebase(sensorId, name) {
                        navController.navigate("plantDetails/$sensorId")
                    }
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SensorConnectionTopBar()
        TextoBusqueda(viewModel, context)
        SensorList(sensors) { sensorId ->
            selectedSensorId = sensorId
            showDialog = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorConnectionTopBar() {
    TopAppBar(title = { Text("Conexión de Sensores") })
}

@Composable
fun TextoBusqueda(viewModel: SensorConnectionViewModel, context: Context) {
    Text(
        text = "Buscando sensores...",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(20.dp))
    Button(onClick = { viewModel.discoverESP32(context) }) {
        Text(text = "Buscar ESP32")
    }
}

@Composable
fun SensorList(sensors: List<Sensor>, onSensorClick: (String) -> Unit) {
    LazyColumn {
        items(sensors) { sensor ->
            SensorItem(sensor) {
                onSensorClick(sensor.id)
            }
        }
    }
}

@Composable
fun SensorItem(sensor: Sensor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DeviceHub,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Sensor ID: ${sensor.id}", fontWeight = FontWeight.Bold)
                Text(text = "IP: ${sensor.ip}")
                Text(text = "Humedad: ${sensor.humedad}")
            }
        }
    }
}

@Composable
fun SensorNameDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Introduce el nombre del sensor", style = MaterialTheme.typography.titleLarge)
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Nombre del sensor") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(text.text)
                        onDismiss()
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
