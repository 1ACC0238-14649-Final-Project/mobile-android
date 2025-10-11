package pe.edu.upc.gigumobile.pull.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class MessageUI(val sender: String, val text: String, val time: String, val isMine: Boolean)

@Composable
fun MessageBubble(message: MessageUI) {
    val bubbleColor = if (message.isMine) Color(0xFF003366) else Color(0xFFF1F1F1)
    val textColor = if (message.isMine) Color.White else Color.Black

    Column(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(message.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        Text(message.time, color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
