package com.example.plantaura2.ui.sensorConnection.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

data class Device(val name: String, val status: String)

@Composable
fun SensorConnectionScreen(viewModel: SensorConnectionViewModel) {
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val permissionsGranted = remember { mutableStateOf(permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted.value = permissions.values.all { it }
    }

    if (!permissionsGranted.value) {
        requestPermissionLauncher.launch(permissions)
    } else {
        MainContent(viewModel = viewModel)
    }
}

@Composable
fun MainContent(viewModel: SensorConnectionViewModel) {

    Column(modifier = Modifier.fillMaxSize()) {
        SensorConnectionTopBar()
        SensorValueDisplay(viewModel = viewModel)
        TextoBusqueda()
        DeviceListDisplay(devices = listOf(
            Device("Dispositivo 1", "Conectado"),
            Device("Dispositivo 2", "Desconectado"),
            Device("Dispositivo 3", "Conectando...")
        ))
    }
}

@Composable
fun SensorValueDisplay(viewModel: SensorConnectionViewModel) {
    val sensorValue by viewModel.readCharacteristicValue.observeAsState(initial = "")
    Log.d("SensorValueDisplay", "Sensor value: $sensorValue")
    Text(text = "Valor del sensor: $sensorValue")
}

@Composable
fun RequestPermissionsView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Se requieren permisos para continuar")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorConnectionTopBar() {
    TopAppBar(title = { Text("Conexión de Sensores") })
}

@Composable
fun TextoBusqueda() {
    Text(
        text = "Buscando sensores...",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun DeviceListDisplay(devices: List<Device>) {
    LazyColumn {
        items(devices) { device ->
            DeviceItem(device)
        }
    }
}

@Composable
fun DeviceItem(device: Device) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
                Text(text = device.name, fontWeight = FontWeight.Bold)
                Text(text = "Estado: ${device.status}")
            }
        }
    }
}