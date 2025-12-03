package pe.edu.upc.gigumobile.users.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import pe.edu.upc.gigumobile.MainActivity

@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    mainActivity: MainActivity? = null
) {
    val state = viewModel.loginState.value
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Configurar callbacks de Google Sign-In
    LaunchedEffect(mainActivity) {
        mainActivity?.setGoogleSignInCallbacks(
            onSuccess = { idToken, email, name, image ->
                viewModel.loginWithGoogle(idToken, email, name, image)
            },
            onError = { error ->
                // El error se mostrará a través del estado del ViewModel
            }
        )
    }

    LaunchedEffect(state.data) {
        if (state.data != null) onLoginSuccess()
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.login(email.trim(), password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }

            // Divider con "o"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = " o ",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Botón de Google Sign-In
            OutlinedButton(
                onClick = { mainActivity?.signInWithGoogle() },
                modifier = Modifier.fillMaxWidth(),
                enabled = mainActivity != null
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Continuar con Google")
                }
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("¿No tienes cuenta? Regístrate")
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.isLoading -> CircularProgressIndicator()
                state.message.isNotEmpty() -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
