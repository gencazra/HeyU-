package com.azrag.heyu.ui.dashboard.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.MatchRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = true,
    val userCards: List<UserProfile> = emptyList(),
    val errorMessage: String? = null,
    val newMatch: UserProfile? = null,
    val isCurrentUserAdmin: Boolean = false,
    val selectedDepartment: String = "Hepsi" // MÜHÜRLENDİ: Fakülte Filtresi
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPotentialMatches()
    }

    // MÜHÜRLENDİ: Bölüm/Fakülte Değiştirme Fonksiyonu
    fun onDepartmentSelected(dept: String) {
        _uiState.update { it.copy(selectedDepartment = dept) }
        loadPotentialMatches()
    }

    fun loadPotentialMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentProfileResult = userRepository.getCurrentUserProfile()

            if (currentProfileResult is Result.Success && currentProfileResult.data != null) {
                val currentUser = currentProfileResult.data!!

                when (val result = matchRepository.getDiscoveryCandidates(currentUser)) {
                    is Result.Success -> {
                        // 1. ADIM: Algoritma - Benzer Profil Skorlaması (% Uyum)
                        var filteredList = result.data?.map { candidate ->
                            val score = currentUser.calculateMatchScoreWith(candidate)
                            candidate.copy(matchScore = score)
                        }?.sortedByDescending { it.matchScore } ?: emptyList()

                        // 2. ADIM: Fakülte/Bölüm Filtreleme
                        if (_uiState.value.selectedDepartment != "Hepsi") {
                            filteredList = filteredList.filter {
                                it.department.contains(_uiState.value.selectedDepartment, ignoreCase = true)
                            }
                        }

                        _uiState.update {
                            it.copy(isLoading = false, userCards = filteredList, errorMessage = null)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    else -> { _uiState.update { it.copy(isLoading = false) } }
                }
            }
        }
    }

    fun onCardSwiped(swipedUser: UserProfile, liked: Boolean) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(userCards = state.userCards.filterNot { it.id == swipedUser.id })
            }
            if (liked) {
                val matchResult = matchRepository.likeUser(swipedUser.id)
                if (matchResult is Result.Success && matchResult.data == true) {
                    _uiState.update { it.copy(newMatch = swipedUser) }
                }
            } else {
                matchRepository.passUser(swipedUser.id)
            }
        }
    }

    fun onMatchDialogDismissed() {
        _uiState.update { it.copy(newMatch = null) }
    }
}
