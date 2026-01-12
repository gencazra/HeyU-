package com.azrag.heyu.ui.dashboard.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.MatchRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()

    private val TAG = "HEYU_DISCOVER"

    init {
        loadPotentialMatches()
    }

    fun add10TestUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val departments = listOf("Software Engineering", "Law", "Medicine", "Architecture", "Psychology")
            val hobbies = listOf("Tennis", "Gaming", "Art", "Chess", "Cooking", "Music", "Photography")

            try {
                for (i in 1..10) {
                    // ID'yi her seferinde tamamen benzersiz yapıyoruz (Saniyeyi de ekledik)
                    val uniqueId = "test_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
                    val testUser = UserProfile(
                        id = uniqueId,
                        displayName = "Student ${Random.nextInt(100, 999)}",
                        email = "$uniqueId@std.yeditepe.edu.tr",
                        age = Random.nextInt(18, 26),
                        department = departments.random(),
                        hobbies = hobbies.shuffled().take(3),
                        photoUrl = "https://ui-avatars.com/api/?name=User+$i&background=random",
                        onboardingComplete = true
                    )
                    userRepository.updateUserProfile(testUser)
                }
                Log.d(TAG, "10 Test users successfully added.")

                // Firebase'in veriyi yayması için 1.5 saniye bekle
                delay(1500)

                // Listeyi temizle ve sıfırdan yükle
                loadPotentialMatches()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding users: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDepartmentSelected(dept: String) {
        _uiState.update { it.copy(selectedDepartment = dept) }
        loadPotentialMatches()
    }

    fun loadPotentialMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentProfileResult = userRepository.getCurrentUserProfile()

            if (currentProfileResult is Result.Success && currentProfileResult.data != null) {
                val currentUser = currentProfileResult.data!!

                // REPOSITORY'DEN ÇEKERKEN FILTREYI KONTROL ET
                when (val result = matchRepository.getDiscoveryCandidates(currentUser)) {
                    is Result.Success -> {
                        val candidates = result.data ?: emptyList()

                        // KRİTİK FİLTRE: onboardingComplete=true olanları getir
                        var filteredList = candidates.filter { it.onboardingComplete }

                        val currentDept = _uiState.value.selectedDepartment
                        if (currentDept != "All" && currentDept != "Hepsi") {
                            filteredList = filteredList.filter {
                                it.department.contains(currentDept, ignoreCase = true)
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userCards = filteredList,
                                errorMessage = if (filteredList.isEmpty()) "No one found. Try 'Add Test Users'!" else null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    else -> _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Profile not found") }
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
        }
    }

    fun onMatchDialogDismissed() {
        _uiState.update { it.copy(newMatch = null) }
    }
}
