package com.azrag.heyu.ui.dashboard.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val userCards: List<UserProfile> = emptyList(),
    val errorMessage: String? = null,
    val newMatch: UserProfile? = null,
    val selectedDepartment: String = "All"
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val userRepository: UserRepository
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
            _uiState.update { it.copy(isLoading = true) }
            val departments = listOf("Yazılım Mühendisliği", "Hukuk", "Tıp", "Mimarlık", "Psikoloji")
            val names = listOf("Ahmet", "Mehmet", "Ayşe", "Fatma", "Can", "Ece", "Bora", "Deniz", "Selin", "Mert")
            val hobbies = listOf("Tennis", "Gaming", "Art", "Chess", "Cooking", "Music", "Photography")

            for (i in 0..9) {
                val testId = "test_user_${System.currentTimeMillis()}_$i"
                val testUser = UserProfile(
                    id = testId,
                    displayName = names[i],
                    email = "test$i@std.yeditepe.edu.tr",
                    age = Random.nextInt(18, 26),
                    department = departments.random(),
                    hobbies = hobbies.shuffled().take(3),
                    photoUrl = "https://i.pravatar.cc/300?u=$testId",
                    onboardingComplete = true,
                    bio = "Hey! Ben bir Yeditepe öğrencisiyim. Tanışalım!"
                )
                userRepository.updateUserProfile(testUser)
            }
            onComplete()
            loadPotentialMatches()
        }
    }

    fun loadPotentialMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = userRepository.getDiscoverUsers()) {
                is Result.Success -> {
                    val candidates = result.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userCards = candidates,
                            errorMessage = if (candidates.isEmpty()) "Etrafında yeni kimse yok!" else null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onCardSwiped(swipedUser: UserProfile, liked: Boolean) {
        _uiState.update { state ->
            state.copy(userCards = state.userCards.filterNot { it.id == swipedUser.id })
        }

        viewModelScope.launch {
            if (liked) {
                val result = userRepository.likeUser(swipedUser.id)
                if (result is Result.Success && result.data == true) {
                    _uiState.update { it.copy(newMatch = swipedUser) }
                }
            } else {
                userRepository.passUser(swipedUser.id)
            }

            if (_uiState.value.userCards.isEmpty()) {
                loadPotentialMatches()
            }
        }
    }
}
