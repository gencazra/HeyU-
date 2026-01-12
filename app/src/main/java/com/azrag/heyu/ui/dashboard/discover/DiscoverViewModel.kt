package com.azrag.heyu.ui.dashboard.discover

import android.util.Log
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
import kotlin.random.Random

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val userCards: List<UserProfile> = emptyList(),
    val errorMessage: String? = null,
    val newMatch: UserProfile? = null, // Virgül hatası burada düzeltildi
    val selectedDepartment: String = "All"
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()

    private val TAG = "HEYU_DISCOVER"

    init {
        loadPotentialMatches()
    }

    /**
     * TEST BUTONU İÇİN: Veritabanına 10 tane hazır Yeditepeli kullanıcı ekler.
     */
    fun add10TestUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val departments = listOf("Yazılım Mühendisliği", "Hukuk", "Tıp", "Mimarlık", "Psikoloji")
            val hobbies = listOf("Tennis", "Gaming", "Art", "Chess", "Cooking", "Music", "Photography")

            for (i in 1..10) {
                val testId = "test_user_${System.currentTimeMillis()}_$i"
                val testUser = UserProfile(
                    id = testId,
                    displayName = "Yeditepe Student $i",
                    email = "test$i@std.yeditepe.edu.tr",
                    age = Random.nextInt(18, 26), // DÜZELTME: .toString() kaldırıldı, artık Int gönderiyor.
                    department = departments.random(),
                    hobbies = hobbies.shuffled().take(3),
                    photoUrl = "https://ui-avatars.com/api/?name=Student+$i&background=random",
                    onboardingComplete = true
                )
                userRepository.updateUserProfile(testUser)
            }
            Log.d(TAG, "10 Test kullanıcısı başarıyla eklendi.")
            loadPotentialMatches()
        }
    }

    fun onDepartmentSelected(dept: String) {
        _uiState.update { it.copy(selectedDepartment = dept) }
        loadPotentialMatches()
    }

    fun loadPotentialMatches() {
        viewModelScope.launch {
            Log.d(TAG, "Loading potential matches...")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentProfileResult = userRepository.getCurrentUserProfile()

            if (currentProfileResult is Result.Success && currentProfileResult.data != null) {
                val currentUser = currentProfileResult.data!!

                when (val result = matchRepository.getDiscoveryCandidates(currentUser)) {
                    is Result.Success -> {
                        val candidates = result.data ?: emptyList()

                        var filteredList = candidates.filter { it.onboardingComplete && it.displayName.isNotBlank() }

                        if (_uiState.value.selectedDepartment != "All" && _uiState.value.selectedDepartment != "Hepsi") {
                            filteredList = filteredList.filter {
                                it.department.contains(_uiState.value.selectedDepartment, ignoreCase = true)
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userCards = filteredList,
                                errorMessage = if (filteredList.isEmpty()) "Etrafında yeni kimse yok!" else null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } else {
                val error = (currentProfileResult as? Result.Error)?.message ?: "Profil yüklenemedi."
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    fun onCardSwiped(swipedUser: UserProfile, liked: Boolean) {
        _uiState.update { state ->
            state.copy(userCards = state.userCards.filterNot { it.id == swipedUser.id })
        }

        viewModelScope.launch {
            if (liked) {
                val matchResult = matchRepository.likeUser(swipedUser.id)
                if (matchResult is Result.Success && matchResult.data == true) {
                    _uiState.update { it.copy(newMatch = swipedUser) }
                }
            } else {
                matchRepository.passUser(swipedUser.id)
            }

            if (_uiState.value.userCards.isEmpty()) {
                loadPotentialMatches()
            }
        }
    }

    fun onMatchDialogDismissed() {
        _uiState.update { it.copy(newMatch = null) }
    }
}
