package pe.edu.upc.gigumobile.users.presentation

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import pe.edu.upc.gigumobile.common.SessionManager
import pe.edu.upc.gigumobile.users.presentation.LoginActivity

@Composable
fun BuyerAccountScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val user = userViewModel.currentUser.value

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF163359)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Account",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        backgroundColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Profile card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture Placeholder
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFEFEF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.image.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user?.image)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default profile",
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${user?.name.orEmpty()} ${user?.lastname.orEmpty()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = user?.email ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // More section
                Text(
                    text = "More",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SessionManager(context).clearSession()
                            userViewModel.logout()
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Log out", color = Color.White)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // My Account section
                Text(
                    text = "My Account",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OutlinedButton(
                    onClick = {
                        // Go to edit account page
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Personal Information")
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Settings section
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OutlinedButton(
                    onClick = {
                        // Language switcher
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Language")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Gray
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Gigs") },
            selected = false,
            onClick = { navController.navigate("gigs") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            selected = false,
            onClick = { navController.navigate("favorites") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
            selected = true,
            onClick = { navController.navigate("account") }
        )
    }
}
