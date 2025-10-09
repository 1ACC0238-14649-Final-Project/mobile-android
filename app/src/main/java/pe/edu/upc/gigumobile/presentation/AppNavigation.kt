package pe.edu.upc.gigumobile.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.gigumobile.users.presentation.LoginScreen
import pe.edu.upc.gigumobile.users.presentation.NotFoundScreen
import pe.edu.upc.gigumobile.users.presentation.RegisterScreen
import pe.edu.upc.gigumobile.users.presentation.UserViewModel

@Composable
fun AppNavigation(
    userViewModel: UserViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = userViewModel,
                onLoginSuccess = { navController.navigate("notfound") {
                    popUpTo("login") { inclusive = true }
                } },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = userViewModel,
                onRegisterSuccess = { navController.navigate("login") },
                onBack = { navController.popBackStack() }
            )
        }

        // 404 route
        composable("notfound") {
            NotFoundScreen(
                onBackToLogin = { navController.navigate("login") {
                    popUpTo(0)
                } }
            )
        }
    }
}
