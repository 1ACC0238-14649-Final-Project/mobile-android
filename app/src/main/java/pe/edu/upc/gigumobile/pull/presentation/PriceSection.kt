package pe.edu.upc.gigumobile.pull.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PriceSectionUI(
    initialLabel: String = "Initial",
    initialPrice: String = "$25",
    currentLabel: String = "Actual",
    currentPrice: String = "$25"
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(initialLabel, fontWeight = FontWeight.Bold)
                Text(initialPrice, fontSize = 20.sp)
            }
            Box(Modifier.height(40.dp).width(1.dp).background(Color.LightGray))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentLabel, fontWeight = FontWeight.Bold)
                Text(currentPrice, fontSize = 20.sp)
            }
        }
    }
}
