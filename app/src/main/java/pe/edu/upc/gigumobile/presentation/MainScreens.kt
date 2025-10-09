package pe.edu.upc.gigumobile.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HomeScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home", style = MaterialTheme.typography.h4)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = false,
            onClick = { navController.navigate("home") { launchSingleTop = true } },
            label = { Text("Home") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Gigs") },
            selected = false,
            onClick = { navController.navigate("gigs") { launchSingleTop = true } },
            label = { Text("Gigs") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            selected = false,
            onClick = { navController.navigate("favorites") { launchSingleTop = true } },
            label = { Text("Favorites") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
            selected = false,
            onClick = { navController.navigate("account") { launchSingleTop = true } },
            label = { Text("Account") }
        )
    }
}
