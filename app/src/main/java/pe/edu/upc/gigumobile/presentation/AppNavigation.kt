package pe.edu.upc.gigumobile.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.gigumobile.users.presentation.LoginScreen
import pe.edu.upc.gigumobile.users.presentation.NotFoundScreen
import pe.edu.upc.gigumobile.users.presentation.RegisterScreen
import pe.edu.upc.gigumobile.users.presentation.UserViewModel
import pe.edu.upc.gigumobile.users.presentation.BuyerAccountScreen
import pe.edu.upc.gigumobile.users.presentation.HomeScreen

@Composable
fun AppNavigation(
    userViewModel: UserViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = userViewModel,
                onLoginSuccess = { navController.navigate("account") {
                    popUpTo("account") { inclusive = true } // Brings you to the account page, change if necessary to home or something
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

        composable("home") {
            // Placeholder home screen
            HomeScreen(navController = navController)
        }
        composable("gigs") {
            // Placeholder gigs screen
            print("Gigs Placeholder")
        }
        composable("favorites") {
            // Placeholder favorites screen
            print("Favorites Placeholder")
        }

        // Account route
        composable("account") {
            BuyerAccountScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }


    }
}
