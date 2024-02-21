package com.example.plantaura2.ui.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plantaura2.R
import com.example.plantaura2.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión
 * @param viewModel ViewModel asociado a la pantalla de inicio de sesión
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Login(Modifier.align(Alignment.Center), viewModel, navController)
    }

}

/**
 * Componente de inicio de sesión
 * @param modifier Modificador para aplicar al componente
 * @param viewModel ViewModel asociado a la pantalla de inicio de sesión
 */
@Composable
fun Login(modifier: Modifier, viewModel: LoginViewModel, navController: NavController) {

    val email: String by viewModel.email.observeAsState(initial = "")
    val password: String by viewModel.password.observeAsState(initial = "")
    val loginEnable: Boolean by viewModel.loginEnable.observeAsState(initial = false)

    val isLoading: Boolean by viewModel.isLoading.observeAsState(initial = false)
    val corrutineScope = rememberCoroutineScope()
    if (isLoading) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    } else {
        Column(modifier = modifier) {
            HeaderImage(Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.padding(16.dp))
            EmailField(email) { viewModel.onLoginChanged(it, password) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordField(password) { viewModel.onLoginChanged(email, it) }
            Spacer(modifier = Modifier.padding(8.dp))
            ForgontPasswordText(Modifier.align(Alignment.End))
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LoginButton(loginEnable) {
                    corrutineScope.launch {
                        viewModel.onLoginSelected(email, password, navController)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Añade aquí el botón de Sign Up
                SignUpButton(
                    onSignUpSelected = { viewModel.onSignUpSelected(navController) },
                    navController = navController
                )
            }
        }
    }
}

/**
 * Botón de registro
 * @param onSignUpSelected Acción a realizar cuando se selecciona el botón de registro
 */
@Composable
fun SignUpButton(onSignUpSelected: () -> Unit, navController: NavController) {
    Button(
        onClick = onSignUpSelected,
        modifier = Modifier
            .width(150.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0x236200EE), // Un color diferente para el botón de Sign Up
            contentColor = White
        )
    ) {
        Text(
            text = "Sign Up",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Botón de inicio de sesión
 * @param loginEnable Indica si el botón de inicio de sesión está habilitado
 * @param onLoginSelected Acción a realizar cuando se selecciona el botón de inicio de sesión
 */
@Composable
fun LoginButton(loginEnable: Boolean, onLoginSelected: () -> Unit) {
    Button(
        onClick = { onLoginSelected() }, modifier = Modifier
            .width(150.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),
            disabledContainerColor = Color(0xFFA4CC76),
            contentColor = White,
            disabledContentColor = White
        ), enabled = loginEnable
    ) {
        Text(
            text = "Login",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

/**
 * Texto de "Olvidó su contraseña"
 * @param modifier Modificador para aplicar al texto
 */
@Composable
fun ForgontPasswordText(modifier: Modifier) {
    Text(
        text = "Forgot Password?",
        modifier = modifier.clickable { },
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = PurpleGrey80
    )
}

/**
 * Campo de texto para la contraseña
 * @param password Contraseña actual
 * @param onTextFieldChanged Acción a realizar cuando cambia el texto del campo
 */
@Composable
fun PasswordField(password: String, onTextFieldChanged: (String) -> Unit) {
    TextField(
        value = password,
        onValueChange = { onTextFieldChanged(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Password") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        maxLines = 1,
    )
}

/**
 * Campo de texto para el correo electrónico
 * @param email Correo electrónico actual
 * @param onTextFieldChanged Acción a realizar cuando cambia el texto del campo
 */
@Composable
fun EmailField(email: String, onTextFieldChanged: (String) -> Unit) {
    TextField(
        value = email, onValueChange = { onTextFieldChanged(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Email") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        maxLines = 1,
    )
}

/**
 * Imagen de encabezado
 * @param modifier Modificador para aplicar a la imagen
 */
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

/*
@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    PlantAura2Theme {
        LoginScreen(viewModel = LoginViewModel())
    }
}
*/