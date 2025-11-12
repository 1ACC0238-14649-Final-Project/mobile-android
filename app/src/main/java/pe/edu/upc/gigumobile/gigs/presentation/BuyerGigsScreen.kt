package pe.edu.upc.gigumobile.gigs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import pe.edu.upc.gigumobile.gigs.presentation.components.GigCard
import pe.edu.upc.gigumobile.gigs.presentation.components.GigsTopBar

@Composable
fun BuyerGigsScreen(
    viewModel: GigViewModel,
    onGigSelected: (String) -> Unit
) {
    val state = viewModel.listState.value

    LaunchedEffect(Unit) {
        viewModel.loadGigs()
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
            ) { CircularProgressIndicator() }

            state.message.isNotEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text(state.message) }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.data) { gig ->
                    GigCard(gig = gig, onClick = { onGigSelected(gig.id) })
                }
            }
        }
    }
}
