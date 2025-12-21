package com.azrag.heyu.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(val isLoading: Boolean = false)

sealed class LoginEvent {
    data class LoginSuccess(val hasProfile: Boolean) : LoginEvent()
    data class LoginError(val message: String) : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // UserRepository içindeki loginUser çağrılır
            when (val result = userRepository.loginUser(email, password)) {
                is Result.Success -> {
                    // result.data (Boolean) değerini doğrudan emit ediyoruz
                    _eventFlow.emit(LoginEvent.LoginSuccess(result.data))
                }
                is Result.Error -> {
                    // Hata mesajını emit ediyoruz
                    _eventFlow.emit(LoginEvent.LoginError(result.message))
                }
                else -> {}
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
