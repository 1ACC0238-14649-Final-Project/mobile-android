package pe.edu.upc.gigumobile.pulls.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.pulls.domain.model.Pull
import pe.edu.upc.gigumobile.pulls.domain.model.PullState
import java.text.SimpleDateFormat
import java.util.*

// Modelo simple para mensajes de chat (puedes crear tu propia estructura de datos)
data class ChatMessage(
    val id: Int,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isCurrentUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullDetailScreen(
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

    // Mensajes de ejemplo (en una implementación real, estos vendrían del backend)
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
                id = 2,
                senderName = "Edward Davidson",
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

            pullDetailState.message.isNotEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(pullDetailState.message)
                }
            }

            pullDetailState.data != null -> {
                val pull = pullDetailState.data
                val gig = gigDetailState.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Sección superior con precios y acciones
                    PullPriceSection(
                        pull = pull,
                        gig = gig,
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
                            // Cambiar el estado del pull a en proceso (aceptado)
                            pullViewModel.updatePull(
                                id = pull.id,
                                newPrice = pull.priceUpdate,
                                newState = PullState.IN_PROCESS,
                                buyerId = pull.buyerId
                            )
                        }
                    )

                    Divider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))

                    // Sección de información del gig
                    if (gig != null) {
                        GigInfoSection(gig = gig)
                        Divider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
                    }

                    // Sección de chat
                    ChatSection(
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
fun PullPriceSection(
    pull: Pull,
    gig: Gig?,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
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
                    text = "$${String.format("%.0f", pull.priceUpdate)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (pull.priceUpdate != pull.priceInit) MaterialTheme.colorScheme.primary else Color.Black
                )
            }

            // Botones
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCancelClick,
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF163A6B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Accept", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GigInfoSection(gig: Gig) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen del gig
        Image(
            painter = rememberAsyncImagePainter(gig.image.ifBlank { null }),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        // Información del gig
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
fun ChatSection(
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
        // Título de Chat
        Text(
            text = "Chat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )

        // Lista de mensajes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
        }

        // Campo de entrada de mensaje
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Agregar archivo */ }) {
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
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isCurrentUser) {
            // Avatar para mensajes de otros usuarios
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