package pe.edu.upc.gigumobile.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.users.presentation.LoginScreen
import pe.edu.upc.gigumobile.users.presentation.NotFoundScreen
import pe.edu.upc.gigumobile.users.presentation.RegisterScreen
import pe.edu.upc.gigumobile.users.presentation.UserViewModel
import pe.edu.upc.gigumobile.pulls.presentation.PullViewModel

@Composable
fun AppNavigation(
    userViewModel: UserViewModel,
    gigViewModel: GigViewModel,
    pullViewModel: PullViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = userViewModel,
                onLoginSuccess = { 
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    } 
                },
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

        composable("main") {
            val currentUser = userViewModel.currentUser.value
            
            when {
                currentUser == null -> {
                    // Redirigir inmediatamente sin mostrar nada
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    // Redirigir en el primer frame
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                else -> {
                    MainScreen(
                        gigViewModel = gigViewModel,
                        pullViewModel = pullViewModel,
                        userViewModel = userViewModel,
                        onLogout = {
                            userViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
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
