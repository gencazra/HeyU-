package com.azrag.heyu.ui.dashboard.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.ChatRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val userCards: List<UserProfile> = emptyList(),
    val errorMessage: String? = null,
    val newMatch: UserProfile? = null,
    val selectedDepartment: String = "All"
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()

    private val TAG = "HEYU_DISCOVER"

    init {
        loadPotentialMatches()
    }

    fun refresh() {
        loadPotentialMatches()
    }

    fun clearMatch() {
        _uiState.update { it.copy(newMatch = null) }
    }

    fun add10TestUsers(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = userRepository.seedDummyUsers()
            
            when (result) {
                is Result.Success -> {
                    delay(2000) 
                    loadPotentialMatches()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> { /* Do nothing */ }
            }
            
            onComplete()
        }
    }

    fun loadPotentialMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = userRepository.getDiscoverUsers()) {
                is Result.Success -> {
                    val candidates = result.data ?: emptyList()
                    Log.d(TAG, "Loaded ${candidates.size} candidates")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userCards = candidates,
                            errorMessage = if (candidates.isEmpty()) "Etrafında yeni kimse yok!" else null
                        )
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Load Error: ${result.message}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onCardSwiped(swipedUser: UserProfile, liked: Boolean) {
        _uiState.update { state ->
            state.copy(userCards = state.userCards.filterNot { it.id == swipedUser.id })
        }

        if (liked) {
            // Kullanıcı sağa kaydırdığı (eklediği) an animasyonu tetikle
            _uiState.update { it.copy(newMatch = swipedUser) }
        }

        viewModelScope.launch {
            try {
                if (liked) {
                    val result = userRepository.likeUser(swipedUser.id)
                    // Eğer gerçek bir eşleşme (mutual match) varsa chat oluştur
                    if (result is Result.Success && result.data == true) {
                        val chatResult = chatRepository.createOrGetChatRoom(swipedUser.id)
                        if (chatResult is Result.Success) {
                            chatRepository.sendTextMessageToRoom(
                                chatRoomId = chatResult.data,
                                receiverId = swipedUser.id,
                                text = "Heyy! 👋"
                            )
                        }
                    }
                } else {
                    userRepository.passUser(swipedUser.id)
                }

                if (_uiState.value.userCards.isEmpty()) {
                    loadPotentialMatches()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Swipe action failed", e)
            }
        }
    }
}
