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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.pulls.domain.model.Pull
import pe.edu.upc.gigumobile.pulls.domain.model.PullState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPullsScreen(
    buyerId: Int,
    viewModel: PullViewModel,
    gigViewModel: GigViewModel,
    onBack: () -> Unit,
    onPullClick: (Int) -> Unit = {}
) {
    val state = viewModel.listState.value
    val navy = Color(0xFF163A6B)
    
    // Estado para almacenar gigs por ID
    val gigsCache = remember { mutableStateMapOf<String, Gig>() }
    
    // Buscar gigs primero en la lista de gigs disponibles de Home
    LaunchedEffect(state.data, gigViewModel.listState.value.data) {
        val availableGigs = gigViewModel.listState.value.data
        
        // Buscar cada gig en la lista disponible
        state.data.forEach { pull ->
            val gigId = pull.gigId.toString()
            val gig = availableGigs.find { it.id == gigId }
            if (gig != null) {
                gigsCache[gigId] = gig
            }
        }
    }
    
    // Cargar gigs individuales que no están en la lista principal
    LaunchedEffect(state.data, gigViewModel.listState.value.data) {
        val availableGigs = gigViewModel.listState.value.data
        
        // Solo cargar gigs que no están en la lista principal
        state.data.forEach { pull ->
            val gigId = pull.gigId.toString()
            if (!gigsCache.containsKey(gigId) && !availableGigs.any { it.id == gigId }) {
                gigViewModel.loadGigDetail(gigId)
            }
        }
    }
    
    // Capturar gigs individuales cuando se cargan
    LaunchedEffect(gigViewModel.detailState.value.data) {
        val gig = gigViewModel.detailState.value.data
        if (gig != null) {
            gigsCache[gig.id] = gig
        }
    }

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
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { pull ->
                        val gig = gigsCache[pull.gigId.toString()]
                        if (gig != null) {
                            GigWithPullCard(
                                gig = gig,
                                pullState = pull.state,
                                onClick = { onPullClick(pull.id) }
                            )
                        } else {
                            // Mientras se carga el gig, mostrar el pull simple
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



