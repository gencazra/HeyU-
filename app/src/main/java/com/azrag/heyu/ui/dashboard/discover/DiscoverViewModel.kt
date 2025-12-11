// ----- DiscoverViewModel.kt DOSYASININ İÇERİĞİ -----

package com.azrag.heyu.ui.dashboard.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Arayüzün dinleyeceği kullanıcı listesi
    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users

    // Yükleme durumu
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUserUid = auth.currentUser?.uid

            firestore.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    val userList = result.documents.mapNotNull { document ->
                        // Firestore dökümanını UserProfile nesnesine dönüştür
                        document.toObject(UserProfile::class.java)
                    }.filter {
                        // Kullanıcının kendi profilini listeden çıkar
                        it.uid != currentUserUid
                    }
                    _users.value = userList
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    // TODO: Hata durumunu arayüze bildir
                    _isLoading.value = false
                }
        }
    }
}
