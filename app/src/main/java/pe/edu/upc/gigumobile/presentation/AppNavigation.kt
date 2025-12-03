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

// IMPORTS for Pull UI
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
                    // Check if the user has already accepted the terms
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
                    // If it declines, perform logout and redirect to the login screen
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
                    // Redirect immediately without displaying anything
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    // Redirect on the first frame
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                else -> {
                    // Check if the user has accepted the terms
                    val termsAccepted = sessionManager?.areTermsAccepted() ?: false
                    
                    if (!termsAccepted) {
                        // If the user has not accepted the terms, redirect to the terms screen
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
                    // Save the visual object in the current back stack
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("pull_ui", pullUi)

                    // Navigate to the Pull screen (UI only)
                    navController.navigate("pull_details")
                }
            )
        }

        // New Route: Visual Screen to Pull Details
        composable("pull_details") {
            // Retrieve the object passed from BuyerGigDetailScreen
            val pullUi = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<PullUi>("pull_ui")

            // Fallback in case it reaches null
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
