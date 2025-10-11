package pe.edu.upc.gigumobile.pulls.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.pulls.domain.model.PullState

@Composable
fun CreatePullScreen(
    gigId: String,
    gigViewModel: GigViewModel,
    pullViewModel: PullViewModel,
    onPullCreated: (Int) -> Unit,
    onCancel: () -> Unit
) {
    val gigState = gigViewModel.detailState.value
    val createState = pullViewModel.createState.value

    LaunchedEffect(gigId) {
        gigViewModel.loadGigDetail(gigId)
    }

    LaunchedEffect(createState.success) {
        if (createState.success && createState.createdPull != null) {
            val buyerId = createState.createdPull.buyerId
            pullViewModel.resetCreateState()
            onPullCreated(buyerId)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            gigState.isLoading || createState.isLoading -> {
                CircularProgressIndicator()
            }
            gigState.message.isNotEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(gigState.message)
                    Button(onClick = onCancel) {
                        Text("Volver")
                    }
                }
            }
            createState.message.isNotEmpty() && !createState.success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = createState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onCancel) {
                        Text("Volver")
                    }
                }
            }
            gigState.data != null -> {
                val gig = gigState.data
                val userViewModel = remember { gigViewModel } // Acceder al userViewModel si está disponible
                
                LaunchedEffect(gig) {
                    // TODO: Obtener el buyerId real del usuario en sesión
                    val buyerId = pullViewModel.getCurrentUserId()
                    val sellerId = gig.sellerId?.toIntOrNull() ?: 0
                    val gigIdInt = gig.id.toIntOrNull() ?: 0

                    pullViewModel.createPull(
                        sellerId = sellerId,
                        gigId = gigIdInt,
                        priceInit = gig.price,
                        priceUpdate = gig.price,
                        buyerId = buyerId,
                        state = PullState.PENDING
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Creando pull para: ${gig.title}")
                    CircularProgressIndicator()
                }
            }
        }
    }
}

