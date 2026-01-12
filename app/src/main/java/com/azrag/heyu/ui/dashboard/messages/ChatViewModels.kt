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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false, // Varsayılanı false yapıyoruz, fonksiyonlar başlatacak
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    var messageText = mutableStateOf("")
    private val chatRoomId: String = savedStateHandle["chatRoomId"] ?: ""
    private var otherUserId: String = ""

    init {
        if (chatRoomId.isNotEmpty()) {
            loadChatData()
        } else {
            loadAllChats()
        }
    }

    /**
     * Tüm konuşmaları (Mesajlar Sekmesi) yükler
     */
    private fun loadAllChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUid = Firebase.auth.currentUser?.uid

            if (currentUid == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "User session not found.") }
                return@launch
            }

            chatRepository.getAllChatsForCurrentUser()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { chats ->
                    if (chats.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, chats = emptyList()) }
                    } else {
                        val enrichedChats = chats.map { chat ->
                            val otherId = chat.participants.find { it != currentUid } ?: ""
                            val otherUserRes = userRepository.getUserProfile(otherId)
                            val otherUser = if (otherUserRes is Result.Success) otherUserRes.data ?: UserProfile() else UserProfile()
                            chat.copy(otherUser = otherUser)
                        }
                        _uiState.update { it.copy(isLoading = false, chats = enrichedChats) }
                    }
                }
        }
    }

    /**
     * Belirli bir sohbet odasının (Chat Screen) detaylarını ve mesajlarını yükler
     */
    private fun loadChatData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUid = Firebase.auth.currentUser?.uid

            if (currentUid == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "User session not found.") }
                return@launch
            }

            val ids = chatRoomId.split("_")
            otherUserId = ids.find { it != currentUid } ?: ""

            if (otherUserId.isNotEmpty()) {
                val otherUserRes = userRepository.getUserProfile(otherUserId)
                val otherUser = if (otherUserRes is Result.Success) otherUserRes.data else null

                combine(
                    userRepository.getUserProfileStream(currentUid),
                    chatRepository.getMessagesFromRoom(chatRoomId)
                ) { currentUser, chatMessages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false, // Veri geldiğinde loading'i kapat
                            currentUserProfile = currentUser,
                            otherUser = otherUser,
                            messages = chatMessages,
                            isUserBlocked = currentUser?.blockedUsers?.contains(otherUserId) ?: false
                        )
                    }
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }.collect()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid chat room.") }
            }
        }
    }

    fun sendTextMessage(textOverride: String? = null) {
        val rawText = textOverride ?: messageText.value
        if (rawText.isBlank() || chatRoomId.isEmpty() || otherUserId.isEmpty()) return

        if (!ModerationManager.isSafe(rawText)) {
            _uiState.update { it.copy(errorMessage = "Message violates guidelines!") }
            return
        }

        val cleanText = ModerationManager.filterText(rawText)
        if (textOverride == null) messageText.value = ""

        viewModelScope.launch {
            chatRepository.sendTextMessageToRoom(
                chatRoomId = chatRoomId,
                receiverId = otherUserId,
                text = cleanText
            )
        }
    }

    fun toggleBlockUser() {
        viewModelScope.launch {
            val result = userRepository.blockUser(otherUserId)
            if (result is Result.Success) {
                _uiState.update { it.copy(errorMessage = "User blocked.", isUserBlocked = true) }
            }
        }
    }
}
