// ----- MyProfileViewModel.kt DOSYASININ İÇERİĞİ -----

package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.ui.dashboard.discover.UserProfile // Bu UserProfile modelini kullanacağız
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyProfileViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Arayüzün dinleyeceği kullanıcı profili
    // Tek bir profil olduğu için Liste değil, null olabilen tek bir nesne tutuyoruz.
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    // Yükleme durumu
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // ViewModel oluşturulur oluşturulmaz kullanıcının kendi verisini çek
        fetchCurrentUserProfile()
    }

    private fun fetchCurrentUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUserUid = auth.currentUser?.uid

            // Eğer kullanıcı girişi yapılmamışsa (teoride imkansız ama kontrol etmek iyidir)
            if (currentUserUid == null) {
                _isLoading.value = false
                // TODO: Hata durumunu yönet
                return@launch
            }

            // Firestore'dan kullanıcının kendi dökümanını çek
            firestore.collection("users").document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Firestore dökümanını UserProfile nesnesine dönüştür
                        val profile = document.toObject(UserProfile::class.java)
                        _userProfile.value = profile
                    } else {
                        // Döküman bulunamadı, bu bir hata durumudur.
                        // TODO: Hata durumunu yönet
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    // Veri çekme başarısız oldu
                    // TODO: Hata durumunu yönet
                    _isLoading.value = false
                }
        }
    }
}
