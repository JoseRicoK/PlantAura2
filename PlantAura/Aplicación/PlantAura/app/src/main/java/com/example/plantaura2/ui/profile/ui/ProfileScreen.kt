package com.example.plantaura2.ui.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.plantaura2.ui.home.ui.BottomNavigationBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, navController: NavController) {
    val userEmail by viewModel.userEmail.observeAsState()
    var newPassword by remember { mutableStateOf("") }
    val passwordChangeMessage: String? by viewModel.passwordChangeMessage.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Perfil",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(22.dp))
        userEmail?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = it,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        HorizontalDivider()  // Línea divisoria
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Cambiar Contraseña:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    BottomNavigationBar(
        navController = navController,
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
            label = { Text("Nueva Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
            onClick = onChangePassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 6.dp)
        ) {
            Text(
                text = "Confirmar Contraseña",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}