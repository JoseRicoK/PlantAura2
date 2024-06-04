package com.example.plantaura2.ui.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.plantaura2.ui.home.ui.BottomNavigationBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.plantaura2.domain.model.Plant

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, navController: NavController) {
    val plants by viewModel.plants.collectAsState()
    var newPassword by remember { mutableStateOf("") }
    val passwordChangeMessage: String? by viewModel.passwordChangeMessage.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        PlantList(plants = plants, onDeletePlant = { plantId ->
            viewModel.onDeletePlantSelected(plantId)
        })
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()  // Línea divisoria
        Spacer(modifier = Modifier.height(16.dp))
        ChangePasswordSection(
            newPassword = newPassword,
            onPasswordChange = { newPassword = it },
            onChangePassword = { viewModel.onChangePassword(newPassword) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        passwordChangeMessage?.let {
            Text(
                text = it,
                color = Color.Blue,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.padding(16.dp))
        }
    }
    BottomNavigationBar(
        onSettingsClick = { viewModel.onSettingsClick(navController) },
        onHomeClick = { viewModel.onHomeClick(navController) },
        onProfileClick = { viewModel.onProfileClick(navController) }
    )
}

@Composable
fun ChangePasswordSection(newPassword: String, onPasswordChange: (String) -> Unit, onChangePassword: () -> Unit) {
    Column {
        OutlinedTextField(
            value = newPassword,
            onValueChange = onPasswordChange,
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onChangePassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Change Password",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
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
            Text("Delete")
        }
    }
}








