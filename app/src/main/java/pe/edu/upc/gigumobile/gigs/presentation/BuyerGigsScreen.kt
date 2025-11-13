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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import pe.edu.upc.gigumobile.gigs.presentation.components.GigCard
import pe.edu.upc.gigumobile.gigs.presentation.components.GigsTopBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun BuyerGigsScreen(
    viewModel: GigViewModel,
    onGigSelected: (String) -> Unit
) {
    val state = viewModel.listState.value

    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadGigs() }

    // debounce: cada vez que cambia 'query', espera 350ms y consulta
    LaunchedEffect(query) {
        delay(350)
        viewModel.loadGigs(searchTerm = query.ifBlank { null })
    }

    Scaffold(
        topBar = { GigsTopBar() }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.message.isNotEmpty() -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
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

                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar por servicio o vendedor...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxSize()
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

                // Lista de gigs
                items(state.data) { gig: Gig ->
                    GigCard(gig = gig, onClick = { onGigSelected(gig.id) })
                }
            }
        }
    }
}

