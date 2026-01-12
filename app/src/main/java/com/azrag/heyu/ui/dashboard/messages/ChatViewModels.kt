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
import com.azrag.heyu.util.ModerationManager
import com.azrag.heyu.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val messages: List<Message> = emptyList(),
    val otherUser: UserProfile? = null,
    val currentUserProfile: UserProfile? = null,
    val errorMessage: String? = null,
    val isUserBlocked: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    var messageText = mutableStateOf("")
    // savedStateHandle üzerinden ID'yi çekiyoruz
    private val chatRoomId: String = savedStateHandle.get<String>("chatRoomId") ?: ""
    private var otherUserId: String = ""

    private val systemUser = UserProfile(
        id = "HeyU_Admin",
        displayName = "HeyU! Team",
        photoUrl = "https://ui-avatars.com/api/?name=HeyU&background=6200EE&color=fff"
    )

    init {
        if (chatRoomId.isNotEmpty()) {
            loadChatData()
        } else {
            loadAllChats()
        }
    }

    private fun loadAllChats() {
        viewModelScope.launch {
            val currentUid = auth.currentUser?.uid ?: return@launch
            
            val welcomeChat = Chat(
                chatRoomId = "system_$currentUid",
                otherUser = systemUser,
                participants = listOf("HeyU_Admin", currentUid),
                lastMessage = "HeyU! ailesine hoş geldin! 👋",
                lastMessageTimestamp = Timestamp.now()
            )
            
            _uiState.update { it.copy(chats = listOf(welcomeChat), isLoading = false) }

            launch { chatRepository.sendWelcomeMessage() }

            chatRepository.getAllChatsForCurrentUser()
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message, isLoading = false) } }
                .collect { chats ->
                    val enriched = chats.map { chat ->
                        async {
                            val otherId = chat.participants.find { it != currentUid } ?: ""
                            val user = if (otherId == "HeyU_Admin" || chat.chatRoomId.startsWith("system_")) {
                                systemUser
                            } else {
                                val res = userRepository.getUserProfile(otherId)
                                if (res is Result.Success) res.data ?: UserProfile() else UserProfile()
                            }
                            chat.copy(otherUser = user)
                        }
                    }.awaitAll().toMutableList()

                    if (enriched.none { it.otherUser.id == "HeyU_Admin" }) {
                        enriched.add(welcomeChat)
                    }
                    
                    val sortedChats = enriched.sortedByDescending { it.lastMessageTimestamp ?: Timestamp.now() }
                    _uiState.update { it.copy(chats = sortedChats, isLoading = false) }
                }
        }
    }

    private fun loadChatData() {
        viewModelScope.launch {
            val currentUid = auth.currentUser?.uid ?: return@launch
            if (chatRoomId.isEmpty()) return@launch

            if (chatRoomId.startsWith("system_")) {
                otherUserId = "HeyU_Admin"
                
                val defaultMsg = Message(
                    id = "welcome_msg",
                    senderId = "HeyU_Admin",
                    text = "HeyU! ailesine hoş geldin! 👋 Yeni insanlarla tanışmaya ve keşfetmeye hemen başlayabilirsin. Keyifli vakit geçirmeni dileriz!",
                    timestamp = Timestamp.now()
                )
                
                _uiState.update { 
                    it.copy(otherUser = systemUser, messages = listOf(defaultMsg), isLoading = false) 
                }

                launch {
                    userRepository.getUserProfileStream(currentUid).collect { profile ->
                        _uiState.update { it.copy(currentUserProfile = profile) }
                    }
                }
                
                launch {
                    chatRepository.getMessagesFromRoom(chatRoomId).collect { msgs ->
                        if (msgs.isNotEmpty()) {
                            _uiState.update { it.copy(messages = msgs) }
                        } else {
                            _uiState.update { it.copy(messages = listOf(defaultMsg)) }
                        }
                    }
                }
            } else {
                val ids = chatRoomId.split("_")
                otherUserId = ids.find { it != currentUid } ?: ""

                if (otherUserId.isNotEmpty()) {
                    _uiState.update { it.copy(isLoading = true) }
                    val otherUserRes = userRepository.getUserProfile(otherUserId)
                    val otherUser = if (otherUserRes is Result.Success) otherUserRes.data else null
                    _uiState.update { it.copy(otherUser = otherUser) }

                    launch {
                        userRepository.getUserProfileStream(currentUid).collect { profile ->
                            _uiState.update { it.copy(currentUserProfile = profile) }
                        }
                    }

                    launch {
                        chatRepository.getMessagesFromRoom(chatRoomId).collect { msgs ->
                            _uiState.update { it.copy(messages = msgs, isLoading = false) }
                        }
                    }
                }
            }
        }
    }

    fun sendTextMessage(textOverride: String? = null) {
        val rawText = textOverride ?: messageText.value
        if (rawText.isBlank() || chatRoomId.isEmpty() || otherUserId.isEmpty() || otherUserId == "HeyU_Admin") return

        if (!ModerationManager.isSafe(rawText)) {
            _uiState.update { it.copy(errorMessage = "Message violates guidelines!") }
            return
        }

        val cleanText = ModerationManager.filterText(rawText)
        if (textOverride == null) messageText.value = ""

        viewModelScope.launch {
            chatRepository.sendTextMessageToRoom(chatRoomId, otherUserId, cleanText)
        }
    }

    fun toggleBlockUser() {
        if (otherUserId == "HeyU_Admin") return
        viewModelScope.launch {
            val result = userRepository.blockUser(otherUserId)
            if (result is Result.Success) {
                _uiState.update { it.copy(errorMessage = "User blocked.", isUserBlocked = true) }
            }
        }
    }
}
