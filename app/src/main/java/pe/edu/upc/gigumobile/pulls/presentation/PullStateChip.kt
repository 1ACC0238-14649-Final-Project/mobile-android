package pe.edu.upc.gigumobile.pulls.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.gigumobile.pulls.domain.model.PullState

@Composable
fun PullStateChip(state: PullState) {
    val (backgroundColor, textColor, label) = when (state) {
        PullState.PENDING -> Triple(
            Color(0xFFFFF3CD),
            Color(0xFF856404),
            "Pendiente"
        )
        PullState.IN_PROCESS -> Triple(
            Color(0xFFCCE5FF),
            Color(0xFF004085),
            "En Proceso"
        )
        PullState.PAYED -> Triple(
            Color(0xFFD4EDDA),
            Color(0xFF155724),
            "Pagado"
        )
        PullState.COMPLETE -> Triple(
            Color(0xFFD1ECF1),
            Color(0xFF0C5460),
            "Completado"
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

