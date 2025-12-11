// Dosya: ui/profile/ProfileViewModel.kt (MODERN YAPI, TAMAMLANMIŞ HALİ)

package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.UserProfile
import com.azrag.heyu.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Kullanıcı arayüzünün (UI) olası durumlarını temsil eden sealed class.
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val userProfile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository // Hilt, repository'yi buraya enjekte edecek
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val userId = userRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = ProfileUiState.Error("Kullanıcı oturumu bulunamadı.")
                return@launch
            }

            try {
                val userProfile = userRepository.getUserProfile(userId)
                if (userProfile != null) {
                    _uiState.value = ProfileUiState.Success(userProfile)
                } else {
                    _uiState.value = ProfileUiState.Error("Profil verileri bulunamadı.")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Profil yüklenirken bir hata oluştu.")
            }
        }
    }

    // ----- YENİ EKLENEN GÜNCELLEME FONKSİYONU -----
    /**
     * Kullanıcının profil bilgilerini (bio, ilgi alanları vb.) günceller.
     * Bu fonksiyon EditProfileScreen tarafından kullanılır.
     * @param newBio Kullanıcının girdiği yeni bio metni.
     * @param newInterests Kullanıcının girdiği yeni ilgi alanları listesi.
     * @param onSuccess İşlem başarılı olunca UI'ı bilgilendirmek için çağrılır.
     * @param onError İşlem başarısız olunca UI'ı bilgilendirmek için çağrılır.
     */
    fun updateUserProfile(
        newBio: String,
        newInterests: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Sadece mevcut state "Success" ise güncelleme yapabiliriz.
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            // Mevcut kullanıcı verilerinin bir kopyasını oluşturup sadece güncellenen alanları değiştiriyoruz.
            // Bu, fullName, email gibi değişmeyen alanların korunmasını sağlar.
            val updatedProfile = currentState.userProfile.copy(
                bio = newBio,
                interests = newInterests
            )

            viewModelScope.launch {
                try {
                    // 1. Güncellenmiş profili Repository aracılığıyla Firestore'a kaydet.
                    userRepository.saveUserProfile(updatedProfile)

                    // 2. Lokal UI state'ini de anında güncelle ki kullanıcı geri döndüğünde yeni bilgileri görsün.
                    _uiState.value = ProfileUiState.Success(updatedProfile)

                    // 3. Arayüze işlemin başarıyla bittiğini haber ver (Toast göstermek, geri gitmek vb. için).
                    onSuccess()
                } catch (e: Exception) {
                    // Firestore'a yazarken bir hata oluşursa, arayüze hata mesajını gönder.
                    onError(e.localizedMessage ?: "Profil güncellenirken bir hata oluştu.")
                }
            }
        } else {
            // Eğer mevcut state Success değilse (örneğin hala yükleniyorsa) güncelleme yapılamaz.
            onError("Güncellenecek kullanıcı verisi henüz hazır değil.")
        }
    }
}
