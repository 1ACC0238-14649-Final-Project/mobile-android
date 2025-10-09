package pe.edu.upc.gigumobile.users.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    viewModel: UserViewModel,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.signupState.value // UIState<Unit>
    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("buyer") } // default

    LaunchedEffect(state.data) {
        if (state.data != null) {
            onRegisterSuccess()
        }
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
            Text(text = "Registro", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lastname,
                onValueChange = { lastname = it },
                label = { Text("Apellido") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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
                onClick = { viewModel.signUp(name, lastname, email.trim(), password, role, "") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }

            TextButton(onClick = onBack) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.isLoading -> CircularProgressIndicator()
                state.message.isNotEmpty() -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
