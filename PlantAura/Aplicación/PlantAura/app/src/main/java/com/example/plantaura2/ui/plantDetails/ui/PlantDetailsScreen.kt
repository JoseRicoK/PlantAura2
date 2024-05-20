package com.example.plantaura2.ui.plantdetails.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController

@Composable
fun PlantDetailsScreen(navController: NavController, plantName: String) {
    val viewModel: PlantDetailsViewModel = viewModel(factory = PlantDetailsViewModelFactory(plantName))
    val plantName by viewModel.plantName.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Detalles de la Planta", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nombre: $plantName", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
