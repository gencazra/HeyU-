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

            // Check if user is logged in AND their email ends with the correct domain
            if (currentUser != null && currentUser.email?.endsWith("@std.yeditepe.edu.tr") == true) {
                // User's email is from Yeditepe, now check if they have a profile
                when (val profileResult = userRepository.getCurrentUserProfile()) {
                    is Result.Success -> {
                        if (profileResult.data != null) {
                            // Profile exists, go to Dashboard
                            _startDestination.value = Screen.Dashboard.route
                        } else {
                            // No profile, go to Onboarding to create one
                            _startDestination.value = Screen.Onboarding1.route
                        }
                    }
                    is Result.Error -> {
                        // Error fetching profile, something is wrong. Log out and go to Login.
                        firebaseAuth.signOut()
                        _startDestination.value = Screen.Login.route
                    }
                    is Result.Loading -> {
                        // While loading, do nothing. The splash screen will continue to show.
                    }
                }
            } else {
                // User is not logged in OR email is not from Yeditepe
                // Log them out just in case and send to Login screen
                if (currentUser != null) {
                    firebaseAuth.signOut()
                }
                _startDestination.value = Screen.Login.route
            }
        }
    }
}
