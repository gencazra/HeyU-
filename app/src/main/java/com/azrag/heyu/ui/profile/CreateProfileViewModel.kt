// Dosya: ui/profile/CreateProfileViewModel.kt

package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.UserProfile
import com.azrag.heyu.data.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val userRepository: UserRepository // Hilt, bu repository'yi otomatik olarak sağlayacak
) : ViewModel() {

    /**
     * Kullanıcıdan alınan bilgileri işler ve veritabanına kaydeder.
     */
    fun saveProfile(
        fullName: String,
        department: String,
        bio: String,
        interests: String, // Virgülle ayrılmış metin
        onSuccess: () -> Unit, // Başarılı olunca çağrılacak fonksiyon
        onError: (String) -> Unit // Hata olunca çağrılacak fonksiyon
    ) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            onError("Kullanıcı oturumu bulunamadı.")
            return
        }

        // Virgülle ayrılmış metni, boşlukları temizleyerek bir listeye dönüştür
        val interestsList = interests.split(',').map { it.trim() }.filter { it.isNotEmpty() }

        // Kaydedilecek UserProfile nesnesini oluştur
        val userProfile = UserProfile(
            id = currentUser.uid,
            fullName = fullName,
            email = currentUser.email ?: "",
            department = department,
            bio = bio,
            interests = interestsList
            // Diğer alanlar şimdilik boş kalabilir
        )

        // Coroutine içinde veritabanı işlemini başlat
        viewModelScope.launch {
            try {
                userRepository.saveUserProfile(userProfile)
                onSuccess() // Başarılı oldu, UI'a haber ver
            } catch (e: Exception) {
                onError(e.message ?: "Profil kaydedilirken bir hata oluştu.")
            }
        }
    }
}
