//----- FeedbackViewModel.kt -----

package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.Feedback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedbackViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Arayüz State'leri
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()

    // Arayüz Event'leri
    fun onMessageChange(newMessage: String) {
        _message.value = newMessage
    }

    fun sendFeedback() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _sendState.value = SendState.Error("Oturum açmış bir kullanıcı bulunamadı.")
            return
        }

        if (_message.value.isBlank()) {
            _sendState.value = SendState.Error("Lütfen bir mesaj yazın.")
            return
        }

        viewModelScope.launch {
            _sendState.value = SendState.Sending
            try {
                // Yeni bir Feedback nesnesi oluştur
                val feedback = Feedback(
                    userId = userId,
                    message = _message.value
                )

                // Firebase'de 'feedback' koleksiyonuna yeni bir belge olarak ekle
                firestore.collection("feedback").add(feedback).await()

                _sendState.value = SendState.Success

            } catch (e: Exception) {
                _sendState.value = SendState.Error(e.message ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }
}

// ViewModel'in durumunu temsil eden mühürlü sınıf
sealed class SendState {
    object Idle : SendState()
    object Sending : SendState()
    object Success : SendState()
    data class Error(val message: String) : SendState()
}

