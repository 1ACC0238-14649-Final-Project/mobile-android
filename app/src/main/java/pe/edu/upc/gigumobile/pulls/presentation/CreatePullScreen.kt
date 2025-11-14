package pe.edu.upc.gigumobile.pulls.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
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
        // Resetear el estado de creación cuando cambia el gigId para permitir múltiples pulls
        pullViewModel.resetCreateState()
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
                val userViewModel = remember { gigViewModel }
                val sellerId = gig.sellerId?.toIntOrNull() ?: 0
                // CRÍTICO: Usar key() para forzar recomposición cuando cambia el gigId
                // Esto asegura que el LaunchedEffect se ejecute con el gig correcto
                key(gigId) {
                    // CRÍTICO: Usar solo el gigId del parámetro de la ruta para evitar problemas con datos obsoletos
                    // El LaunchedEffect debe ejecutarse cuando el gig cargado coincide con el gigId de la ruta
                    // IMPORTANTE: Agregar gig.id y gigState.isLoading como dependencias para que se ejecute cuando el gig se carga
                    LaunchedEffect(gigId, gig.id, gigState.isLoading) {
                        // Esperar a que el gig se cargue y verificar que corresponde al gigId de la ruta
                        // Solo ejecutar si el gig coincide y no está cargando, y no hay un pull ya creado exitosamente
                        if (gig.id == gigId && !gigState.isLoading && !createState.success && !createState.isLoading) {
                            // Solo intentar crear el pull si no hay un estado de carga o éxito previo
                            if (sellerId > 0) {
                            val buyerId = pullViewModel.getCurrentUserId()
                            // Validar que el buyerId sea válido antes de continuar
                            if (buyerId <= 0) {
                                pullViewModel.createState.value = pullViewModel.createState.value.copy(
                                    isLoading = false,
                                    success = false,
                                    message = "No se pudo identificar tu usuario. Por favor, cierra sesión y vuelve a iniciar sesión."
                                )
                            } else {
                                // Convertir gigId del parámetro (String) a Int
                                // USAR SIEMPRE EL gigId DEL PARÁMETRO, NO gig.id
                                val paramGigIdInt = gigId.toIntOrNull()
                                if (paramGigIdInt != null && paramGigIdInt > 0) {
                                    // Verificar que el gig.id coincida con el parámetro (doble verificación)
                                    val gigIdFromGig = gig.id.toIntOrNull()
                                    if (gigIdFromGig == paramGigIdInt) {
                                        pullViewModel.createPull(
                                            sellerId = sellerId,
                                            gigId = paramGigIdInt, // Usar el gigId del parámetro
                                            priceInit = gig.price,
                                            priceUpdate = gig.price,
                                            buyerId = buyerId,
                                            state = PullState.PENDING
                                        )
                                    } else {
                                        // Los IDs no coinciden - el gig cargado no corresponde a la ruta
                                        pullViewModel.createState.value = pullViewModel.createState.value.copy(
                                            isLoading = false,
                                            success = false,
                                            message = "Error: El gig cargado no coincide con la ruta. Intenta nuevamente."
                                        )
                                    }
                                } else {
                                    // Mostramos un error si el gigId no es válido
                                    pullViewModel.createState.value = pullViewModel.createState.value.copy(
                                        isLoading = false,
                                        success = false,
                                        message = "El ID del gig no es válido (${gigId}). No se puede crear el pull."
                                    )
                                }
                            }
                            } else if (sellerId <= 0) {
                                // Mostramos un error en la vista si el sellerId es inválido
                                pullViewModel.createState.value = pullViewModel.createState.value.copy(
                                    isLoading = false,
                                    success = false,
                                    message = "No es posible crear el pull porque falta información válida del vendedor. Intenta nuevamente más tarde."
                                )
                            }
                        }
                    }
                }
                if (sellerId > 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Creando pull para: ${gig.title}")
                        Text("sellerId: ${gig.sellerId} | gigId: ${gig.id}", 
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall)
                        CircularProgressIndicator()
                    }
                } // Si sellerId <= 0 no mostramos spinner, el mensaje/error aparece arriba
            }
        }
    }
}

