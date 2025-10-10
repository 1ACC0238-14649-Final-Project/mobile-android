package pe.edu.upc.gigumobile.pull.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageInputBar(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Type a message...") },
            modifier = Modifier
                .weight(1f),
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                // Fondo del TextField (antes: containerColor)
                focusedContainerColor = Color(0xFFF7F8FA),
                unfocusedContainerColor = Color(0xFFF7F8FA),
                disabledContainerColor = Color(0xFFF7F8FA),
                errorContainerColor = Color(0xFFF7F8FA),

                // Quitar la línea/indicador
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            maxLines = 3
        )

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = { /* visual only */ },
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF003366), CircleShape)
        ) {
            Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
        }
    }
}
