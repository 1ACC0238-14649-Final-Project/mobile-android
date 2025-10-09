package pe.edu.upc.gigumobile.users.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotFoundScreen(
    onBackToLogin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "404", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Página no encontrada", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Has iniciado sesión correctamente — esta es una pantalla 404 de ejemplo.\nPulsa volver para regresar al login.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBackToLogin) {
                Text("Volver al login")
            }
        }
    }
}
