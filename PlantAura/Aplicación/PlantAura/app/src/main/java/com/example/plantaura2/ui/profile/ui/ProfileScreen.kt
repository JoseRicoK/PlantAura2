package com.example.plantaura2.ui.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantaura2.ui.home.ui.BottomNavigationBar

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel(), navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "Profile Screen",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
    BottomNavigationBar(
        onSettingsClick = { viewModel.onSettingsClick(navController) },
        onHomeClick = { viewModel.onHomeClick(navController) },
        onProfileClick = { viewModel.onProfileClick(navController) }
    )
}
