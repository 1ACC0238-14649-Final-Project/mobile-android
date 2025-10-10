package pe.edu.upc.gigumobile.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.gigumobile.gigs.presentation.BuyerGigDetailScreen
import pe.edu.upc.gigumobile.gigs.presentation.BuyerGigsScreen
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.users.presentation.LoginScreen
import pe.edu.upc.gigumobile.users.presentation.NotFoundScreen
import pe.edu.upc.gigumobile.users.presentation.RegisterScreen
import pe.edu.upc.gigumobile.users.presentation.UserViewModel

// ⬇️ IMPORTS para Pull UI
import pe.edu.upc.gigumobile.pull.presentation.PullUi
import pe.edu.upc.gigumobile.pull.presentation.PullDetailsScreen

@Composable
fun AppNavigation(
    userViewModel: UserViewModel,
    gigViewModel: GigViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = userViewModel,
                onLoginSuccess = {
                    navController.navigate("buyer_gigs") {
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

        composable("buyer_gigs") {
            BuyerGigsScreen(
                viewModel = gigViewModel,
                onGigSelected = { id -> navController.navigate("buyer_gig_detail/$id") }
            )
        }

        composable("buyer_gig_detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            BuyerGigDetailScreen(
                gigId = id,
                viewModel = gigViewModel,
                onBack = { navController.popBackStack() },
                onBuyNow = { pullUi: PullUi ->
                    // Guardar el objeto visual en el back stack actual
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("pull_ui", pullUi)

                    // Navegar a la pantalla Pull (solo UI)
                    navController.navigate("pull_details")
                }
            )
        }

        // ⬇️ Nueva ruta: pantalla visual de Pull Details
        composable("pull_details") {
            // Recuperar el objeto pasado desde BuyerGigDetailScreen
            val pullUi = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<PullUi>("pull_ui")

            // Fallback por si llega nulo
            val safe = pullUi ?: PullUi(
                title = "Gig title",
                description = "No description provided.",
                imageUrl = null,
                initialPriceLabel = "$0",
                currentPriceLabel = "$0"
            )

            PullDetailsScreen(
                data = safe,
                onBack = { navController.popBackStack() }
            )
        }

        // 404 route
        composable("notfound") {
            NotFoundScreen(
                onBackToLogin = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
