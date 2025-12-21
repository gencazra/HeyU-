package com.azrag.heyu.ui.dashboard.messages

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azrag.heyu.data.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageText by viewModel.messageText
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.otherUser?.displayName ?: "Sohbet", fontWeight = FontWeight.Bold)
                        Text(
                            text = uiState.otherUser?.department ?: "Yeditepe Üniversitesi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Kullanıcıyı Engelle") },
                            onClick = {
                                viewModel.toggleBlockUser()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Rapor Et (Uygunsuz İçerik)") },
                            onClick = {
                                // ViewModel'e raporlama fonksiyonu eklenebilir
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Column(modifier = Modifier.navigationBarsPadding()) {

                    // MÜHÜRLENDİ: Güvenlik için ilk mesajda Hey! önerisi
                    AnimatedVisibility(
                        visible = uiState.messages.isEmpty() && !uiState.isLoading,
                        enter = fadeIn() + expandVertically()
                    ) {
                        SuggestionChip(
                            onClick = { viewModel.sendTextMessage("Hey! 👋") },
                            label = { Text("Selam ver: Hey! 👋", fontSize = 12.sp) },
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                            shape = CircleShape
                        )
                    }

                    // Moderasyon Hatası Gösterimi
                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    ChatInputArea(
                        text = messageText,
                        onValueChange = { viewModel.messageText.value = it },
                        onSend = { viewModel.sendTextMessage() }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            reverseLayout = true,
            contentPadding = PaddingValues(16.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(
                    message = message,
                    isMine = message.senderId == uiState.currentUserProfile?.id
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = color,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            modifier = Modifier.padding(vertical = 4.dp).widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ChatInputArea(text: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Mesaj yazın...") },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.width(8.dp))
        FloatingActionButton(
            onClick = onSend,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Send, contentDescription = "Gönder", tint = Color.White)
        }
    }
}
