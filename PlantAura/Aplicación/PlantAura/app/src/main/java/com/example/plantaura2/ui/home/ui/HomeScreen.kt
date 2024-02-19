package com.example.plantaura2.ui.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.plantaura2.ui.theme.PlantAura2Theme

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
    PlantAura2Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            WelcomeText()
        }
    }
}

@Composable
fun WelcomeText() {
    Text(text = "Bienvenido a la pantalla de inicio")
}


