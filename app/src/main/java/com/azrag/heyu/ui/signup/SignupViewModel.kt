package com.azrag.heyu.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SignupUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignupSuccess: Boolean = false
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    fun onSignupClick(email: String, pass: String, fullName: String) {
        val cleanEmail = email.trim()

        if (!cleanEmail.endsWith("@std.yeditepe.edu.tr") && !cleanEmail.endsWith("@yeditepe.edu.tr")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Yeditepe email address!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(cleanEmail, pass).await()
                val user = result.user

                if (user != null) {
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = fullName
                    }
                    user.updateProfile(profileUpdates).await()

                    val userMap = mapOf(
                        "uid" to user.uid,
                        "email" to cleanEmail,
                        "displayName" to fullName,
                        "isAdmin" to false,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    firestore.collection("users").document(user.uid).set(userMap).await()
                }

                _uiState.update { it.copy(isLoading = false, isSignupSuccess = true) }

            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password") == true -> "Password must be at least 6 characters."
                    e.message?.contains("already in use") == true -> "This email is already in use."
                    else -> e.localizedMessage ?: "An error occurred."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        }
    }
}
