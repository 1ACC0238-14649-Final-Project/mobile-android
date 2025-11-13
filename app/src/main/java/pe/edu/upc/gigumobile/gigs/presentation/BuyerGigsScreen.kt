package pe.edu.upc.gigumobile.gigs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import pe.edu.upc.gigumobile.gigs.presentation.components.GigCard
import pe.edu.upc.gigumobile.gigs.presentation.components.GigsTopBar

@Composable
fun BuyerGigsScreen(
    viewModel: GigViewModel,
    onGigSelected: (String) -> Unit
) {
    val state = viewModel.listState.value
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadGigs() }

    LaunchedEffect(query) {
        delay(350)
        viewModel.loadGigs(searchTerm = query.ifBlank { null })
    }

    Scaffold(
        topBar = { GigsTopBar() }
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

            state.message.isNotEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message)
            }

            else -> {
                val gigs = state.data
                val isSearching = query.isNotBlank()

                val bestSelling = gigs.take(3)
                val newGigs = gigs.takeLast(3)

                val tips = listOf(
                    "Puedes negociar con el freelancer para llegar a un precio final.",
                    "Establece expectativas claras desde el inicio para evitar malentendidos.",
                    "Usa el chat integrado para negociar y aclarar dudas antes de contratar."
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(bottom = 90.dp)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {

                    item {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Buscar por servicio...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            trailingIcon = {
                                if (query.isNotBlank()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.loadGigs(searchTerm = query.ifBlank { null })
                                    defaultKeyboardAction(ImeAction.Search)
                                }
                            )
                        )
                    }

                    if (isSearching) {
                        if (gigs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Sin resultados",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "No encontramos gigs que coincidan con \"$query\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "${gigs.size} resultado${if (gigs.size != 1) "s" else ""} encontrado${if (gigs.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(gigs) { gig ->
                                GigCard(
                                    gig = gig,
                                    onClick = { onGigSelected(gig.id) }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    } else {
                        item {
                            InfoBanner(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }

                        item {
                            AutoRotatingTipCard(
                                tips = tips,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }

                        if (bestSelling.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Más vendidos",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            item {
                                HorizontalGigsRow(
                                    gigs = bestSelling,
                                    onGigSelected = onGigSelected
                                )
                            }
                        }

                        if (newGigs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Nuevos gigs",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            item {
                                HorizontalGigsRow(
                                    gigs = newGigs,
                                    onGigSelected = onGigSelected
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalGigsRow(
    gigs: List<Gig>,
    onGigSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(gigs) { gig ->
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .wrapContentHeight()
            ) {
                GigCard(
                    gig = gig,
                    onClick = { onGigSelected(gig.id) }
                )
            }
        }
    }
}

@Composable
private fun InfoBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0052CC),
                        Color(0xFF2F80ED)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Conecta con talento joven",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Encuentra estudiantes freelancers verificados listos para ayudarte con tus proyectos",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun AutoRotatingTipCard(
    tips: List<String>,
    modifier: Modifier = Modifier,
    rotationIntervalMillis: Long = 5000L
) {
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(rotationIntervalMillis)
            currentIndex = (currentIndex + 1) % tips.size
        }
    }

    val tip = tips.getOrNull(currentIndex).orEmpty()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEAF4FF))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row {
                tips.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex)
                                    Color(0xFF1E88E5)
                                else
                                    Color(0xFFB3E0FF)
                            )
                    )
                    if (index < tips.lastIndex) Spacer(Modifier.width(4.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "CONSEJO",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1E88E5)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = tip,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1E2A3B)
        )
    }
}