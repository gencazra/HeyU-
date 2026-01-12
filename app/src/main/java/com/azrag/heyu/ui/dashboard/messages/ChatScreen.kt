package com.azrag.heyu.ui.dashboard.messages

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

// Figma'daki oval başlık yapısı için özel şekil
val HeaderWaveShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.75f)
    quadraticBezierTo(
        size.width / 2f, size.height,
        0f, size.height * 0.75f
    )
    close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageText by viewModel.messageText
    var showMenu by remember { mutableStateOf(false) }
    val isSystemChat = uiState.otherUser?.id == "HeyU_Admin" || uiState.otherUser?.id == "heyu_system"

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                // Arka plan rengi (Primary) ve oval kesim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(HeaderWaveShape)
                        .background(MaterialTheme.colorScheme.primary)
                )

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "heyU!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))

                        if (!isSystemChat) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert, 
                                    contentDescription = "Menu", 
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }

                    // Profil Bölümü (İsim ve Resim)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = uiState.otherUser?.photoUrl?.ifBlank { R.drawable.ic_default_profile } ?: R.drawable.ic_default_profile
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(85.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(3.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "*${uiState.otherUser?.displayName ?: "User"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (isSystemChat) "Official Support" else (uiState.otherUser?.department ?: "Yeditepe University"),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isSystemChat) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 4.dp
                ) {
                    ChatInputArea(
                        text = messageText,
                        onValueChange = { viewModel.messageText.value = it },
                        onSend = { viewModel.sendTextMessage() }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "You cannot reply to this automated message.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            reverseLayout = true,
            contentPadding = PaddingValues(16.dp)
        ) {
            items(uiState.messages) { message ->
                val isMine = message.senderId == uiState.currentUserProfile?.id
                MessageBubble(
                    message = message,
                    isMine = isMine
                )
            }
        }
    }

    if (showMenu) {
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Block User") },
                onClick = {
                    viewModel.toggleBlockUser()
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Report") },
                onClick = { showMenu = false },
                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    
    // Gece/Gündüz moduna göre MaterialTheme renkleri kullanılır
    val bubbleColor = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = message.timestamp?.let { sdf.format(it.toDate()) } ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isMine) 20.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 20.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = timeStr,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ChatInputArea(text: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Mesaj yazın...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(25.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .size(48.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
        }
        @Composable
        fun MessageListContent(
            padding: PaddingValues,
            messages: List<Message>,
            currentUserId: String?
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                reverseLayout = true, // Yeni mesajlar altta görünür
                contentPadding = PaddingValues(16.dp)
            ) {
                items(messages) { message ->
                    val isMine = message.senderId == currentUserId
                    MessageBubble(
                        message = message,
                        isMine = isMine
                    )
                }
            }
        }

        @Composable
        fun MessageBubble(message: Message, isMine: Boolean) {
            // Tasarımdaki renk ve hizalama mantığı
            val bubbleColor = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val textColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
            val timeStr = message.timestamp?.let { sdf.format(it.toDate()) } ?: ""

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
            ) {
                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isMine) 20.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 20.dp
                    ),
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
