package com.example.plantaura2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plantaura2.ui.home.ui.HomeScreen
import com.example.plantaura2.ui.home.ui.HomeViewModel
import com.example.plantaura2.ui.login.ui.LoginScreen
import com.example.plantaura2.ui.login.ui.LoginViewModel
import com.example.plantaura2.ui.plantdetails.ui.PlantDetailsScreen
import com.example.plantaura2.ui.profile.ui.ProfileScreen
import com.example.plantaura2.ui.profile.ui.ProfileViewModel
import com.example.plantaura2.ui.questionHub.ui.HubScreen
import com.example.plantaura2.ui.questionHub.ui.QuestionHubViewModel
import com.example.plantaura2.ui.sensorConnection.ui.SensorConnectionScreen
import com.example.plantaura2.ui.sensorConnection.ui.SensorConnectionViewModel
import com.example.plantaura2.ui.settings.ui.SettingsScreen
import com.example.plantaura2.ui.settings.ui.SettingsViewModel
import com.example.plantaura2.ui.signup.ui.SignUpScreen
import com.example.plantaura2.ui.signup.ui.SignUpViewModel

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    signUpViewModel: SignUpViewModel,
    homeViewModel: HomeViewModel,
    hubViewModel: QuestionHubViewModel,
    sensorConnectionViewModel: SensorConnectionViewModel,
    profileViewModel: ProfileViewModel,
    settingsViewModel: SettingsViewModel
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
        composable(Screen.SensorConnection.route) {
            SensorConnectionScreen(viewModel = sensorConnectionViewModel, navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = profileViewModel, navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = settingsViewModel, navController = navController)
        }
        composable(
            route = "${Screen.PlantDetails.route}/{plantName}",
            arguments = listOf(navArgument("plantName") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantName = backStackEntry.arguments?.getString("plantName") ?: ""
            PlantDetailsScreen(navController = navController, plantName = plantName)
        }
    }
}
