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
            _uiState.update { it.copy(errorMessage = "L\u00FCtfen ge\u00E7erli bir Yeditepe maili giriniz!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Kullanıcı Oluştur
                val result = auth.createUserWithEmailAndPassword(cleanEmail, pass).await()
                val user = result.user

                if (user != null) {
                    // 2. İsim Güncelleme (Türkçe Karakter Desteği)
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = fullName
                    }
                    user.updateProfile(profileUpdates).await()

                    // 3. Firestore Dokümanı (Türkçe Karakter Safe)
                    val userMap = mapOf(
                        "uid" to user.uid,
                        "email" to cleanEmail,
                        "displayName" to fullName,
                        "isAdmin" to false,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    firestore.collection("users").document(user.uid).set(userMap).await()
                }

                // 4. BAŞARI: Flag'i ateşle
                _uiState.update { it.copy(isLoading = false, isSignupSuccess = true) }

            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password") == true -> "\u015Eifre en az 6 karakter olmal\u0131d\u0131r."
                    e.message?.contains("already in use") == true -> "Bu e-posta zaten kullan\u0131l\u0131yor."
                    else -> e.localizedMessage ?: "Bir hata olu\u015Ftu."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        }
    }
}
