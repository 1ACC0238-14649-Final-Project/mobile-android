package pe.edu.upc.gigumobile.gigs.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import pe.edu.upc.gigumobile.pull.presentation.PullUi // ⬅️ IMPORTANTE

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BuyerGigDetailScreen(
    gigId: String,
    viewModel: GigViewModel,
    onBack: () -> Unit,
    onBuyNow: (PullUi) -> Unit   // ⬅️ ahora recibe PullUi
) {
    val state = viewModel.detailState.value

    LaunchedEffect(gigId) { viewModel.loadGigDetail(gigId) }

    val navy = Color(0xFF163A6B)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gig Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            state.data?.let { gig ->
                Button(
                    onClick = {
                        // Construimos el objeto visual para PullDetailsScreen
                        val pullUi = PullUi(
                            title = gig.title,
                            description = gig.description.ifBlank { "No description provided." },
                            imageUrl = gig.image.ifBlank { null },
                            initialPriceLabel = "$${gig.price}",
                            currentPriceLabel = "$${gig.price}"
                        )
                        onBuyNow(pullUi)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Buy Now!  $${state.data.price}")
                }
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.message.isNotEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) { Text(state.message) }

            state.data != null -> DetailContent(
                gig = state.data,
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    gig: Gig,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Image(
            painter = rememberAsyncImagePainter(gig.image.ifBlank { null }),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        Text(gig.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        if (gig.category.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(gig.category, style = MaterialTheme.typography.labelMedium)
        }

        Spacer(Modifier.height(8.dp))

        if (gig.sellerName.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(gig.sellerName, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(16.dp))
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(gig.description.ifBlank { "No description provided." })

        // Stats (chips)
        Spacer(Modifier.height(16.dp))
        Text("Stats", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (gig.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gig.tags.forEach { tag ->
                    AssistChip(onClick = { }, label = { Text(tag) })
                }
            }
        } else {
            Text("No tags.")
        }

        // Extras
        Spacer(Modifier.height(16.dp))
        Text("Extras", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val extras: List<String> = buildList {
            gig.deliveryDays?.let { add("Delivery: $it day(s)") }
            if (gig.extraFeatures.isNotEmpty()) addAll(gig.extraFeatures)
        }

        if (extras.isEmpty()) {
            Text("No extras.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                extras.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(item, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp)) // espacio para el botón inferior
    }
}
