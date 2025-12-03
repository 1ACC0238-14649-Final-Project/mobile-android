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
import pe.edu.upc.gigumobile.gigs.presentation.BuyerGigDetailScreen
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.users.presentation.LoginScreen
import pe.edu.upc.gigumobile.users.presentation.NotFoundScreen
import pe.edu.upc.gigumobile.users.presentation.RegisterScreen
import pe.edu.upc.gigumobile.users.presentation.TermsAndConditionsScreen
import pe.edu.upc.gigumobile.users.presentation.UserViewModel
import pe.edu.upc.gigumobile.pulls.presentation.PullViewModel
import pe.edu.upc.gigumobile.common.SessionManager

// ⬇️ IMPORTS para Pull UI
import pe.edu.upc.gigumobile.pull.presentation.PullUi
import pe.edu.upc.gigumobile.pull.presentation.PullDetailsScreen
import pe.edu.upc.gigumobile.MainActivity
import android.content.Context

@Composable
fun AppNavigation(
    userViewModel: UserViewModel,
    gigViewModel: GigViewModel,
    pullViewModel: PullViewModel,
    mainActivity: MainActivity? = null,
    context: Context? = null
) {
    val navController = rememberNavController()
    val sessionManager = context?.let { SessionManager(it) }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = userViewModel,
                onLoginSuccess = {
                    // Verificar si el usuario ya aceptó los términos
                    val termsAccepted = sessionManager?.areTermsAccepted() ?: false
                    if (termsAccepted) {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("terms") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                mainActivity = mainActivity
            )
        }

        composable("terms") {
            TermsAndConditionsScreen(
                onAccept = {
                    sessionManager?.setTermsAccepted(true)
                    navController.navigate("main") {
                        popUpTo("terms") { inclusive = true }
                    }
                },
                onDecline = {
                    // Si rechaza, hacer logout y volver al login
                    userViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
                    // Verificar si el usuario aceptó los términos
                    val termsAccepted = sessionManager?.areTermsAccepted() ?: false
                    
                    if (!termsAccepted) {
                        // Si no aceptó términos, redirigir a la pantalla de términos
                        LaunchedEffect(Unit) {
                            navController.navigate("terms") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    } else {
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
