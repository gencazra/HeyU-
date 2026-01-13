package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MyProfileUiState {
    object Loading : MyProfileUiState()
    data class Success(val profile: UserProfile) : MyProfileUiState()
    data class Error(val message: String) : MyProfileUiState()
    object LoggedOut : MyProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyProfileUiState>(MyProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init { loadCurrentUserProfile() }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUserProfileStream().collect { profile ->
                if (profile != null) {
                    _uiState.value = MyProfileUiState.Success(profile)
                } else {
                    _uiState.value = MyProfileUiState.Error("Profil yüklenemedi.")
                }
            }
        }
    }

    // GENEL GÜNCELLEME FONKSİYONU: Her şeyi bu kurtaracak
    fun updateProfileField(update: (UserProfile) -> UserProfile) {
        val currentState = _uiState.value
        if (currentState is MyProfileUiState.Success) {
            viewModelScope.launch {
                val updatedProfile = update(currentState.profile)
                userRepository.updateUserProfile(updatedProfile)
            }
        }
    }

    fun logout() {
        Firebase.auth.signOut()
        _uiState.value = MyProfileUiState.LoggedOut
    }
}
