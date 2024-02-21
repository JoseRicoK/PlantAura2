package com.example.plantaura2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plantaura2.ui.home.ui.HomeScreen
import com.example.plantaura2.ui.home.ui.HomeViewModel
import com.example.plantaura2.ui.login.ui.LoginScreen
import com.example.plantaura2.ui.login.ui.LoginViewModel
import com.example.plantaura2.ui.questionHub.ui.HubScreen
import com.example.plantaura2.ui.questionHub.ui.QuestionHubViewModel
import com.example.plantaura2.ui.signup.ui.SignUpScreen
import com.example.plantaura2.ui.signup.ui.SignUpViewModel

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    signUpViewModel: SignUpViewModel,
    homeViewModel: HomeViewModel,
    hubViewModel: QuestionHubViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(viewModel = loginViewModel, navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(viewModel = signUpViewModel, navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }
        composable(Screen.Hub.route) {
            HubScreen()
        }
    }
}
