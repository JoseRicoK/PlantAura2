package com.example.plantaura2.ui.plantDetails.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plantaura2.ui.plantDetails.ui.PlantDetailsViewModel

@Composable
fun PlantDetailsScreen(sensorId: String, viewModel: PlantDetailsViewModel = viewModel(factory = PlantDetailsViewModelFactory(sensorId))) {
    val sensorData by viewModel.sensorData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        sensorData?.let { sensor ->
            Text(text = "Sensor ID: ${sensor.id}", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(text = "IP: ${sensor.ip}", fontSize = 20.sp)
            Text(text = "Humedad: ${sensor.humedad}%", fontSize = 20.sp)
        } ?: run {
            CircularProgressIndicator()
        }
    }
}
