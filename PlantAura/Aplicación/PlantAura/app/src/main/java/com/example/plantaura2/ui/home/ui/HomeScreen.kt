package com.example.plantaura2.ui.home.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plantaura2.navigation.Screen
import kotlinx.coroutines.launch
import java.io.File

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
            viewModel = viewModel,
            imageDirectory = viewModel.imageDirectory
        )
        BottomNavigationBar(
            navController = navController,
            onSettingsClick = { viewModel.onSettingsClick(navController) },
            onHomeClick = { viewModel.onHomeClick(navController) },
            onProfileClick = { viewModel.onProfileClick(navController) }
        )
    }
}

@Composable
fun BodyContent(
    modifier: Modifier = Modifier,
    plantNames: List<Plant>,
    navController: NavController,
    viewModel: HomeViewModel,
    imageDirectory: File
) {
    Column(modifier = modifier) {
        PlantList(modifier, plantNames, navController, viewModel, imageDirectory)
    }
}

@Composable
fun PlantList(
    modifier: Modifier = Modifier,
    plantNames: List<Plant>,
    navController: NavController,
    viewModel: HomeViewModel,
    imageDirectory: File
) {
    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(plantNames.size) { index ->
            PlantItem(plantNames[index], imageDirectory) {
                viewModel.onPlantSelected(navController, plantNames[index].name)
            }
        }
    }
}

@Composable
fun PlantItem(planta: Plant, imageDirectory: File, onClick: () -> Unit) {
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
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Establecer una altura fija para el Row
                .background(Color(0xFFF5F5F5)) // Color de fondo blanco roto
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp) // Establecer un ancho fijo para la imagen
                    .fillMaxHeight()
            ) {
                if (planta.hasImage) {
                    // Mostrar imagen de la planta
                    val imagePath = "${imageDirectory.path}/sensor_${planta.id}.jpg"
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen de ${planta.name}",
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(100.dp), // Asegurar que la imagen tenga un ancho fijo
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Mostrar icono predeterminado
                    Icon(
                        imageVector = Icons.Filled.Spa,
                        contentDescription = "Planta",
                        modifier = Modifier
                            //.size(60.dp) // Ajustar el tamaño del icono
                            .align(Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFFF5F5F5)), // Degradado de transparente a blanco roto
                                startX = 100f,
                                endX = 280f // Ajustar para mover el final del gradiente
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = planta.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward, // Flecha indicando que se puede pulsar
                contentDescription = "Flecha indicando que se puede pulsar",
                modifier = Modifier
                    //.size(24.dp)
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp)
            )
        }
    }
}










@Composable
fun BottomNavigationBar(
    navController: NavController,
    onSettingsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

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
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Ajustes",
                        tint = if (currentRoute == Screen.Settings.route) Color(0xFF821ACC) else Color.Gray
                    )
                }
                IconButton(onClick = onHomeClick) {
                    Icon(
                        imageVector = Icons.Filled.Spa,
                        contentDescription = "Plantas",
                        tint = if (currentRoute == Screen.Home.route) Color(0xFF821ACC) else Color.Gray
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Perfil",
                        tint = if (currentRoute == Screen.Profile.route) Color(0xFF821ACC) else Color.Gray
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, onPlusSelected: () -> Unit = {}) {
    TopAppBar(
        title = { Text(
            text = "Plantas",
            fontWeight = FontWeight.Bold
        ) },
        actions = {
            IconButton(onClick = onPlusSelected ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir planta")
            }
        }
    )
}