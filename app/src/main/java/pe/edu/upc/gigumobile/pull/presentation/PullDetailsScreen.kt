package pe.edu.upc.gigumobile.pull.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.pulls.presentation.PullViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullDetailsScreen(
    data: PullUi,
    onBack: () -> Unit = {}
) {
    // Versión que acepta PullUi directamente (para el flujo de Buy Now)
    PullDetailsScreenContent(
        pullUi = data,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullDetailsScreen(
    pullId: Int,
    pullViewModel: PullViewModel,
    gigViewModel: GigViewModel,
    onBack: () -> Unit = {}
) {
    // Versión que carga el pull real desde el backend (para ver pulls existentes)
    val pullState = pullViewModel.detailState.value
    val gigState = gigViewModel.detailState.value

    LaunchedEffect(pullId) {
        pullViewModel.loadPullDetail(pullId)
    }

    // Cargar el gig asociado cuando se carga el pull
    LaunchedEffect(pullState.data?.gigId) {
        pullState.data?.gigId?.let { gigId ->
            gigViewModel.loadGigDetail(gigId.toString())
        }
    }

    // Convertir los datos reales a PullUi para usar los componentes existentes
    val pullUi = remember(pullState.data, gigState.data) {
        val pull = pullState.data
        val gig = gigState.data
        if (pull != null && gig != null) {
            PullUi(
                title = gig.title,
                description = gig.description.ifBlank { "No description provided." },
                imageUrl = gig.image.ifBlank { null },
                initialPriceLabel = "$${String.format("%.2f", pull.priceInit)}",
                currentPriceLabel = "$${String.format("%.2f", pull.priceUpdate)}"
            )
        } else {
            null
        }
    }

    when {
        pullState.isLoading -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Pull Details", color = Color.White, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF003366))
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        pullState.message.isNotEmpty() && pullState.data == null -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Pull Details", color = Color.White, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF003366))
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = pullState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onBack) {
                            Text("Volver")
                        }
                    }
                }
            }
        }

        pullUi != null -> {
            PullDetailsScreenContent(
                pullUi = pullUi,
                onBack = onBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PullDetailsScreenContent(
    pullUi: PullUi,
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pull Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF003366))
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    PriceSectionUI(
                        initialPrice = pullUi.initialPriceLabel,
                        currentPrice = pullUi.currentPriceLabel
                    )
                }
                item { GigDescriptionCardUI(title = pullUi.title, price = pullUi.initialPriceLabel) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { /* visual only */ },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF003366))
                        ) { Text("Cancel", fontWeight = FontWeight.Medium) }
                    }
                }
                item { Text("Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                items(sampleMessages) { message ->
                    MessageBubble(message)
                    Spacer(Modifier.height(4.dp))
                }
                item { Spacer(Modifier.height(72.dp)) }
            }

            MessageInputBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            )
        }
    }
}
