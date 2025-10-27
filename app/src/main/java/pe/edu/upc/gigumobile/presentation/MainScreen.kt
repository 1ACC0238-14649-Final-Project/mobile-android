package pe.edu.upc.gigumobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.gigumobile.gigs.presentation.BuyerGigDetailScreen
import pe.edu.upc.gigumobile.gigs.presentation.BuyerGigsScreen
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.pulls.presentation.CreatePullScreen
import pe.edu.upc.gigumobile.pulls.presentation.MyPullsScreen
import pe.edu.upc.gigumobile.pulls.presentation.PullViewModel

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object MyPulls : BottomNavItem("my_pulls_nav", "My Pulls", Icons.Default.Favorite)
    object Profile : BottomNavItem("profile", "Perfil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    gigViewModel: GigViewModel,
    pullViewModel: PullViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navy = Color(0xFF163A6B)

    Scaffold(
        bottomBar = {
            // Mostrar el bottom bar en todas las rutas excepto las de creación y detalle
            val showBottomBar = currentRoute == "home" || 
                                currentRoute == "my_pulls_nav" || 
                                currentRoute == "profile"
            
            if (showBottomBar) {
                NavigationBar(
                    containerColor = navy
                ) {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.MyPulls,
                        BottomNavItem.Profile
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo("home") { 
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                BuyerGigsScreen(
                    viewModel = gigViewModel,
                    onGigSelected = { id -> navController.navigate("gig_detail/$id") }
                )
            }

            composable("gig_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                BuyerGigDetailScreen(
                    gigId = id,
                    viewModel = gigViewModel,
                    onBack = { navController.popBackStack() },
                    onBuyNow = { navController.navigate("create_pull/$id") }
                )
            }

            composable("create_pull/{gigId}") { backStackEntry ->
                val gigId = backStackEntry.arguments?.getString("gigId") ?: ""
                CreatePullScreen(
                    gigId = gigId,
                    gigViewModel = gigViewModel,
                    pullViewModel = pullViewModel,
                    onPullCreated = { buyerId ->
                        navController.navigate("my_pulls_nav") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable("my_pulls_nav") {
                LaunchedEffect(Unit) {
                    val userId = pullViewModel.getCurrentUserId()
                    pullViewModel.loadPullsByBuyerId(userId)
                }

                MyPullsScreen(
                    buyerId = 0, // Este parámetro no se usa, el viewModel ya tiene los datos
                    viewModel = pullViewModel,
                    gigViewModel = gigViewModel,
                    onBack = { 
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onPullClick = { pullId ->
                        // Aquí puedes navegar a detalle si lo implementas
                    }
                )
            }

            composable("profile") {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val navy = Color(0xFF163A6B)
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = navy,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}

