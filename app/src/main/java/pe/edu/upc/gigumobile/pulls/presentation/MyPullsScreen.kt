package pe.edu.upc.gigumobile.pulls.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.gigumobile.pulls.domain.model.Pull
import pe.edu.upc.gigumobile.pulls.domain.model.PullState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPullsScreen(
    buyerId: Int,
    viewModel: PullViewModel,
    onBack: () -> Unit,
    onPullClick: (Int) -> Unit = {}
) {
    val state = viewModel.listState.value
    val navy = Color(0xFF163A6B)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Pulls") },
                navigationIcon = {
                    // Solo mostrar botón back si onBack no es vacío
                    if (onBack != {}) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.message.isNotEmpty() && state.data.isEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message)
            }

            state.data.isEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes pulls aún.", style = MaterialTheme.typography.bodyLarge)
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { pull ->
                        PullCard(
                            pull = pull,
                            onClick = { onPullClick(pull.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PullCard(
    pull: Pull,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pull #${pull.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                PullStateChip(state = pull.state)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gig ID: ${pull.gigId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Precio inicial: $${String.format("%.2f", pull.priceInit)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (pull.priceUpdate != pull.priceInit) {
                        Text(
                            text = "Precio actualizado: $${String.format("%.2f", pull.priceUpdate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

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

