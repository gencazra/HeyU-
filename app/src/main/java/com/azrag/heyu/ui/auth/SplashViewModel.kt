package com.azrag.heyu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import com.azrag.heyu.util.Screen
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser


            if (currentUser != null && currentUser.email?.endsWith("@std.yeditepe.edu.tr") == true) {

                when (val profileResult = userRepository.getCurrentUserProfile()) {
                    is Result.Success -> {
                        if (profileResult.data != null) {

                            _startDestination.value = Screen.Dashboard.route
                        } else {

                            _startDestination.value = Screen.Onboarding1.route
                        }
                    }
                    is Result.Error -> {

                        firebaseAuth.signOut()
                        _startDestination.value = Screen.Login.route
                    }
                    is Result.Loading -> {

                    }
                }
            } else {

                if (currentUser != null) {
                    firebaseAuth.signOut()
                }
                _startDestination.value = Screen.Login.route
            }
        }
    }
}
