package com.azrag.heyu.ui.dashboard.messages

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.Chat
import com.azrag.heyu.data.model.Message
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.ChatRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val isLoading: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val errorMessage: String? = null
)

data class ChatDetailUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val otherUser: UserProfile? = null,
    val currentUserProfile: UserProfile? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState = _uiState.asStateFlow()

    private val systemUser = UserProfile(
        id = "HeyU_Admin",
        displayName = "HeyU! Team",
        photoUrl = "" 
    )

    init {
        loadAllChats()
    }

    private fun loadAllChats() {
        val currentUid = auth.currentUser?.uid
        
        // Varsayılan hoş geldin mesajını hemen listeye ekleyelim (Boş görünmesin)
        if (currentUid != null) {
            val welcomePlaceholder = Chat(
                chatRoomId = "system_$currentUid",
                otherUser = systemUser,
                participants = listOf("HeyU_Admin", currentUid),
                lastMessage = "HeyU! ailesine hoş geldin! 👋",
                lastMessageTimestamp = Timestamp.now()
            )
            _uiState.update { it.copy(chats = listOf(welcomePlaceholder), isLoading = true) }
        }

        viewModelScope.launch {
            if (currentUid == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            // Arka planda Firestore'a mesajı gönder/güncelle
            try { chatRepository.sendWelcomeMessage() } catch (e: Exception) {}

            chatRepository.getAllChatsForCurrentUser()
                .catch { e -> _uiState.update { it.copy(isLoading = false) } }
                .collect { chats ->
                    val enriched = chats.map { chat ->
                        async {
                            val oId = chat.participants.find { it != currentUid }
                            if (oId == null || oId == "HeyU_Admin" || chat.chatRoomId.startsWith("system_")) {
                                chat.copy(otherUser = systemUser)
                            } else {
                                val res = userRepository.getUserProfile(oId)
                                val user = if (res is Result.Success) res.data ?: UserProfile(id = oId, displayName = "User") 
                                           else UserProfile(id = oId, displayName = "User")
                                chat.copy(otherUser = user)
                            }
                        }
                    }.awaitAll().toMutableList()

                    // Sistem mesajı listede yoksa ekle
                    if (enriched.none { it.chatRoomId.startsWith("system_") }) {
                        enriched.add(0, Chat(
                            chatRoomId = "system_$currentUid",
                            otherUser = systemUser,
                            participants = listOf("HeyU_Admin", currentUid),
                            lastMessage = "HeyU! ailesine hoş geldin! 👋",
                            lastMessageTimestamp = Timestamp.now()
                        ))
                    }
                    
                    val sorted = enriched.sortedByDescending { it.lastMessageTimestamp ?: Timestamp.now() }
                    _uiState.update { it.copy(chats = sorted, isLoading = false) }
                }
        }
    }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState = _uiState.asStateFlow()

    var messageText = mutableStateOf("")
    
    private val chatRoomId: String = savedStateHandle.get<String>("chatRoomId") ?: ""
    private var otherUserId: String = ""

    private val welcomeMsg = Message(
        id = "welcome_system",
        senderId = "HeyU_Admin",
        text = "HeyU! ailesine hoş geldin! 👋 Yeni insanlarla tanışmaya ve keşfetmeye hemen başlayabilirsin. Keyifli vakit geçirmeni dileriz!",
        timestamp = Timestamp.now()
    )

    init {
        if (chatRoomId.isNotBlank()) {
            loadChatData()
        }
    }

    private fun loadChatData() {
        val currentUid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (chatRoomId.startsWith("system_")) {
                otherUserId = "HeyU_Admin"
                _uiState.update { it.copy(otherUser = UserProfile(id = "HeyU_Admin", displayName = "HeyU! Team"), messages = listOf(welcomeMsg)) }
            } else {
                val parts = chatRoomId.split("_")
                otherUserId = parts.find { it != currentUid } ?: ""
                if (otherUserId.isNotEmpty()) {
                    val res = userRepository.getUserProfile(otherUserId)
                    if (res is Result.Success) _uiState.update { it.copy(otherUser = res.data) }
                }
            }

            chatRepository.getMessagesFromRoom(chatRoomId).collect { msgs ->
                val finalMessages = if (chatRoomId.startsWith("system_") && msgs.isEmpty()) listOf(welcomeMsg) else msgs
                _uiState.update { it.copy(messages = finalMessages, isLoading = false) }
            }
        }
    }

    fun sendTextMessage() {
        val text = messageText.value
        if (text.isBlank() || chatRoomId.isBlank() || otherUserId.isBlank() || otherUserId == "HeyU_Admin") return
        messageText.value = ""
        viewModelScope.launch { chatRepository.sendTextMessageToRoom(chatRoomId, otherUserId, text) }
    }

    fun toggleBlockUser() {
        if (otherUserId.isEmpty() || otherUserId == "HeyU_Admin") return
        viewModelScope.launch {
            userRepository.blockUser(otherUserId)
        }
    }
}
