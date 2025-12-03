package pe.edu.upc.gigumobile.pulls.presentation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.pulls.domain.model.Pull
import pe.edu.upc.gigumobile.pulls.domain.model.PullState
import java.text.SimpleDateFormat
import java.util.*

// Modelo simple para mensajes de chat
data class ChatMessage(
    val id: Int,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isCurrentUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullDetailScreenNew(
    pullId: Int,
    pullViewModel: PullViewModel,
    gigViewModel: GigViewModel,
    onBack: () -> Unit
) {
    val pullDetailState = pullViewModel.detailState.value
    val gigDetailState = gigViewModel.detailState.value
    val navy = Color(0xFF163A6B)

    // Estado para el mensaje de chat
    var messageText by remember { mutableStateOf("") }

    // Estado para el precio editable
    var editedPrice by remember { mutableStateOf("") }

    // Estado para mostrar el diálogo de confirmación
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Estado para controlar si se está actualizando
    var isUpdating by remember { mutableStateOf(false) }

    // Mensajes de ejemplo
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                id = 1,
                senderName = "David Wayne",
                message = "Does this update fix error 352 for the Engineer character?",
                timestamp = "10:11",
                isCurrentUser = false
            ),
            ChatMessage(
                id = 1,
                senderName = "David Wayne",
                message = "Oh!\nThey fixed it and upgraded the security further. 🚀",
                timestamp = "10:14",
                isCurrentUser = false
            ),
            ChatMessage(
                id = 3,
                senderName = "You",
                message = "Great! 😊",
                timestamp = "10:20",
                isCurrentUser = true
            )
        )
    }

    LaunchedEffect(pullId) {
        pullViewModel.loadPullDetail(pullId)
    }

    LaunchedEffect(pullDetailState.data?.gigId) {
        pullDetailState.data?.gigId?.let { gigId ->
            gigViewModel.loadGigDetail(gigId.toString())
        }
    }

    // Inicializar el precio editable cuando se carga el pull
    LaunchedEffect(pullDetailState.data) {
        pullDetailState.data?.let { pull ->
            if (editedPrice.isEmpty()) {
                editedPrice = pull.priceUpdate.toString()
            }
        }
    }

    // Diálogo de confirmación (condicional según estado)
    if (showConfirmDialog) {
        val pull = pullDetailState.data
        val isPending = pull?.state == PullState.PENDING
        val isInProcess = pull?.state == PullState.IN_PROCESS

        if (isPending) {
            // Diálogo para PENDING: modificar precio
            val currentPrice = pull?.priceUpdate ?: 0.0
            val newPrice = editedPrice.toDoubleOrNull() ?: currentPrice
            val hasChanges = newPrice != currentPrice

            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        "Confirmar actualización",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        if (hasChanges) {
                            Text("¿Quieres modificar el precio actual?")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Precio anterior: $${"%.2f".format(currentPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                "Precio nuevo: $${"%.2f".format(newPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("No hay cambios en el precio.")
                            Text("¿Deseas continuar?")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            if (hasChanges && pull != null) {
                                isUpdating = true
                                // Actualizar el precio manteniendo el mismo estado
                                pullViewModel.updatePull(
                                    id = pull.id,
                                    newPrice = newPrice,
                                    newState = pull.state,
                                    buyerId = pull.buyerId
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF163A6B)
                        )
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showConfirmDialog = false
                        }
                    ) {
                        Text("No")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        } else if (isInProcess) {
            // Diálogo para IN_PROCESS: completar trabajo
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        "Confirmar finalización",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("El freelancer ya completó el trabajo. ¿Deseas marcar este pull como completado?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            if (pull != null) {
                                isUpdating = true
                                // Cambiar estado a COMPLETE manteniendo el precio actual
                                pullViewModel.updatePull(
                                    id = pull.id,
                                    newPrice = pull.priceUpdate,
                                    newState = PullState.COMPLETE,
                                    buyerId = pull.buyerId
                                )
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF163A6B)
                        )
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showConfirmDialog = false
                        }
                    ) {
                        Text("No")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    // Mostrar mensaje cuando se actualiza exitosamente
    LaunchedEffect(pullDetailState.message) {
        if (pullDetailState.message.contains("actualizado") && isUpdating) {
            isUpdating = false
            // Recargar el pull para obtener los datos actualizados
            pullViewModel.loadPullDetail(pullId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pull Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        when {
            pullDetailState.isLoading || gigDetailState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            pullDetailState.message.isNotEmpty() && pullDetailState.data == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(pullDetailState.message)
                        Button(onClick = onBack) {
                            Text("Volver")
                        }
                    }
                }
            }

            pullDetailState.data != null -> {
                val pull = pullDetailState.data
                val gig = gigDetailState.data

                // Determinar el estado del Pull
                val isPending = pull.state == PullState.PENDING
                val isInProcess = pull.state == PullState.IN_PROCESS
                val isComplete = pull.state == PullState.COMPLETE
                val showActions = isPending || isInProcess

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Sección superior con precios y acciones
                    if (showActions) {
                        PullPriceSectionNew(
                            pull = pull,
                            gig = gig,
                            editedPrice = editedPrice,
                            onPriceChange = { newValue ->
                                // Solo permitir números y un punto decimal
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    editedPrice = newValue
                                }
                            },
                            onCancelClick = {
                                // Cambiar el estado del pull a completado (cancelado)
                                pullViewModel.updatePull(
                                    id = pull.id,
                                    newPrice = pull.priceUpdate,
                                    newState = PullState.COMPLETE,
                                    buyerId = pull.buyerId
                                )
                                onBack()
                            },
                            onAcceptClick = {
                                // Mostrar diálogo de confirmación
                                showConfirmDialog = true
                            },
                            isUpdating = isUpdating,
                            isPending = isPending
                        )
                    } else {
                        // Solo mostrar precios sin botones (para COMPLETE)
                        SimplePriceDisplay(
                            initialPrice = pull.priceInit,
                            currentPrice = pull.priceUpdate
                        )
                    }

                    Divider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))

                    // Sección de información del gig
                    if (gig != null) {
                        GigInfoSectionNew(gig = gig)
                        Divider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
                    }

                    // Sección de chat
                    ChatSectionNew(
                        messages = chatMessages,
                        messageText = messageText,
                        onMessageTextChange = { messageText = it },
                        onSendMessage = {
                            if (messageText.isNotBlank()) {
                                val newMessage = ChatMessage(
                                    id = chatMessages.size + 1,
                                    senderName = "You",
                                    message = messageText,
                                    timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                    isCurrentUser = true
                                )
                                chatMessages.add(newMessage)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PullPriceSectionNew(
    pull: Pull,
    gig: Gig?,
    editedPrice: String,
    onPriceChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    isUpdating: Boolean,
    isPending: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Precio inicial
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Initial",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.0f", pull.priceInit)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Precio actual - CAMPO EDITABLE (solo si isPending)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Actual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = editedPrice,
                    onValueChange = onPriceChange,
                    enabled = isPending, // ← NUEVO: solo editable en PENDING
                    modifier = Modifier.width(100.dp),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPending) MaterialTheme.colorScheme.primary else Color.Gray
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    prefix = { Text("$", style = MaterialTheme.typography.titleLarge) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray,
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Botones
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCancelClick,
                    enabled = !isUpdating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF163A6B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF163A6B)),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }

                Button(
                    onClick = onAcceptClick,
                    enabled = !isUpdating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF163A6B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(100.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Accept", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePriceDisplay(
    initialPrice: Double,
    currentPrice: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Precio inicial
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Initial",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.0f", initialPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Precio actual
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Actual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.0f", currentPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GigInfoSectionNew(gig: Gig) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(gig.image.ifBlank { null }),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = gig.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$${gig.price}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChatSectionNew(
    messages: List<ChatMessage>,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Text(
            text = "Chat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatMessageItemNew(message = message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF163A6B)
                )
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                placeholder = { Text("Type a message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF163A6B), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatMessageItemNew(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isCurrentUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.first().toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!message.isCurrentUser) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isCurrentUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isCurrentUser) 4.dp else 16.dp
                ),
                color = if (message.isCurrentUser) Color(0xFF2196F3) else Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isCurrentUser) Color.White else Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (message.isCurrentUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Y",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}