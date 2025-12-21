package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
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

    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            _uiState.value = MyProfileUiState.Loading
            when (val result = userRepository.getCurrentUserProfile()) {
                is Result.Success -> {
                    val profile = result.data ?: UserProfile(id = Firebase.auth.currentUser?.uid ?: "")
                    _uiState.value = MyProfileUiState.Success(profile)
                }
                is Result.Error -> {
                    _uiState.value = MyProfileUiState.Error(result.message ?: "Profil yüklenemedi.")
                }
                else -> {}
            }
        }
    }

    fun logout() {
        Firebase.auth.signOut()
        _uiState.value = MyProfileUiState.LoggedOut
    }
}
