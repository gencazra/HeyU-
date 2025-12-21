// Dosya Yolu: ui/auth/SplashViewModel.kt
package com.azrag.heyu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.util.Screen
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            // Şema [✓]: 2 saniye bekle
            delay(2000)

            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // Şema [✓]: Giriş yapmışsa Dashboard
                _startDestination.value = Screen.Dashboard.route
            } else {
                // Şema [✓]: Yapmamışsa Login
                _startDestination.value = Screen.Login.route
            }
        }
    }
}
