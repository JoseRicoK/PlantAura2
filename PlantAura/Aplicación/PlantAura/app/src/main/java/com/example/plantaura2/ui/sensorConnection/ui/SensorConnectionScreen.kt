package com.example.plantaura2.ui.sensorConnection.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantaura2.MainActivity

@Composable
fun SensorConnectionScreen(
    viewModel: SensorConnectionViewModel = viewModel(),
    plantTypeViewModel: PlantTypeViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val sensors by viewModel.sensors.collectAsState()
    val plantTypes by plantTypeViewModel.plantTypes.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedSensorId by remember { mutableStateOf<String?>(null) }

    if (showDialog) {
        SensorNameDialog(
            plantTypes = plantTypes,
            onDismiss = { showDialog = false },
            onSave = { name, type ->
                selectedSensorId?.let { sensorId ->
                    viewModel.saveSensorToFirebase(sensorId, name, type) {
                        navController.navigate("plantDetails/$sensorId")
                    }
                }
            },
            onTakePhoto = {
                // Llamar a la función para abrir la cámara
                (context as MainActivity).openCamera()
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
    TopAppBar(title = { Text("Conexión de Sensores", fontWeight = FontWeight.Bold) })
}

@Composable
fun TextoBusqueda(viewModel: SensorConnectionViewModel, context: Context) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        Button(
            onClick = { viewModel.discoverESP32(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Buscar ESP32")
        }
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
                Text(text = "${sensor.sensor}", fontWeight = FontWeight.Bold)
                Text(text = "Id: ${sensor.id}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorNameDialog(plantTypes: List<String>, onDismiss: () -> Unit, onSave: (String, String) -> Unit, onTakePhoto: () -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(plantTypes.firstOrNull() ?: "") }

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

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedType,
                        onValueChange = {},
                        label = { Text("Tipo de planta") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        plantTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(text.text, selectedType)
                        onDismiss()
                    }) {
                        Text("Guardar")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onTakePhoto) {
                    Text("Tomar Foto")
                }
            }
        }
    }
}
