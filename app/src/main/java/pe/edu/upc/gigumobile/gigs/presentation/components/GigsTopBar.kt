package pe.edu.upc.gigumobile.gigs.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.WindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GigsTopBar(
    title: String = "Gigs",
    containerColor: Color = Color(0xFF163A63),
    contentColor: Color = Color.White
) {
    //Pinta la status bar
    val sysUi = rememberSystemUiController()
    SideEffect { sysUi.setStatusBarColor(containerColor, darkIcons = false) }

    CenterAlignedTopAppBar(
        modifier = Modifier.height(56.dp),
        windowInsets = WindowInsets(0),
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                )
            }
        },
        navigationIcon = {},
        actions = {},
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor
        )
    )
}
