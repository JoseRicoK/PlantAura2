package com.example.plantaura2.ui.home.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
) {
    val plantNames by viewModel.plantNames.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            viewModel.loadPlantNames() // Recarga los nombres de las plantas
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopBar(navController = navController, onPlusSelected = { viewModel.onPlusSelected(navController) })
        BodyContent(
            modifier = Modifier.weight(1f),
            plantNames = plantNames,
            navController = navController,
            viewModel = viewModel
        )
        BottomNavigationBar(
            onSettingsClick = { viewModel.onSettingsClick(navController) },
            onHomeClick = { viewModel.onHomeClick(navController) },
            onProfileClick = { viewModel.onProfileClick(navController) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, onPlusSelected: () -> Unit = {}) {
    TopAppBar(
        title = { Text("Plantas") },
        actions = {
            IconButton(onClick = onPlusSelected ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir planta")
            }
        }
    )
}

@Composable
fun BodyContent(modifier: Modifier = Modifier, plantNames: List<Plant>, navController: NavController, viewModel: HomeViewModel) {
    Column(modifier = modifier) {
        PlantList(modifier, plantNames, navController, viewModel)
    }
}

@Composable
fun PlantList(modifier: Modifier = Modifier, plantNames: List<Plant>, navController: NavController, viewModel: HomeViewModel) {
    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(plantNames.size) { index ->
            PlantItem(plantNames[index]) {
                viewModel.onPlantSelected(navController, plantNames[index].name)
            }
        }
    }
}

@Composable
fun PlantItem(planta: Plant, onClick: () -> Unit) {
    val borderColor = if (planta.revive) Color.Red else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Spa,
                contentDescription = "Planta",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = planta.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Detalles",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    onSettingsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Ajusta el padding para que 'flote'
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
                IconButton(onClick = onHomeClick) {
                    Icon(Icons.Filled.Spa, contentDescription = "Plantas")
                }
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Filled.Person, contentDescription = "Perfil")
                }
            }
        }
    }
}
