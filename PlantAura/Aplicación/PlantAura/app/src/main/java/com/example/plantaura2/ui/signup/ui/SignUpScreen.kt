package com.example.plantaura2.ui.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plantaura2.R
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(viewModel: SignUpViewModel, navController: NavController) {
    val navigation: String? by viewModel.navigation.observeAsState()

    LaunchedEffect(navigation) {
        if (navigation != null) {
            navController.navigate(navigation!!)
            viewModel.onNavigationHandled() // Reset navigation state after handling it
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SignUp(Modifier.align(Alignment.Center), viewModel)
    }
}

@Composable
fun SignUp(modifier: Modifier, viewModel: SignUpViewModel) {
    val email: String by viewModel.email.observeAsState("")
    val password: String by viewModel.password.observeAsState("")
    val confirmPassword: String by viewModel.confirmPassword.observeAsState("")
    val signUpEnable: Boolean by viewModel.signUpEnable.observeAsState(false)
    val isLoading: Boolean by viewModel.isLoading.observeAsState(false)
    val coroutineScope = rememberCoroutineScope()

    if (isLoading) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    } else {
        Column(modifier = modifier) {
            HeaderImage(Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.padding(16.dp))
            EmailField(email) { viewModel.onEmailChanged(it) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordField(password) { viewModel.onPasswordChanged(it) }
            Spacer(modifier = Modifier.padding(4.dp))
            ConfirmPasswordField(confirmPassword) { viewModel.onConfirmPasswordChanged(it) }
            Spacer(modifier = Modifier.padding(8.dp))
            SignUpButton(signUpEnable) {
                coroutineScope.launch {
                    viewModel.onSignUpSelected()
                }
            }
        }
    }
}

@Composable
fun EmailField(email: String, onEmailChanged: (String) -> Unit) {
    TextField(
        value = email,
        onValueChange = onEmailChanged,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Email") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

@Composable
fun PasswordField(password: String, onPasswordChanged: (String) -> Unit) {
    TextField(
        value = password,
        onValueChange = onPasswordChanged,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Password") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
fun ConfirmPasswordField(confirmPassword: String, onConfirmPasswordChanged: (String) -> Unit) {
    TextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChanged,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Confirm Password") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
fun SignUpButton(signUpEnable: Boolean, onSignUpSelected: () -> Unit) {
    Button(
        onClick = { onSignUpSelected() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE),
            contentColor = Color.White
        ),
        enabled = signUpEnable
    ) {
        Text(
            text = "Sign Up",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HeaderImage(modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.plantaura_logo_fondo_removebg),
        contentDescription = "PlantAura Logo",
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    )
}
