// Dosya: ui/profile/MyProfileViewModel.kt (ESKİ YAPI, TAMAMLANMIŞ HALİ)

package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.UserProfile // Yeni veri modelimizi import ediyoruz
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Bu ViewModel, EditProfileScreen'in beklediği UiState'i kullanacak şekilde güncellendi
class MyProfileViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val usersCollection = firestore.collection("users")

    // Arayüzün dinleyeceği state. Artık tek bir UiState kullanıyoruz.
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        // ViewModel oluşturulur oluşturulmaz kullanıcının kendi verisini çek
        fetchCurrentUserProfile()
    }

    private fun fetchCurrentUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading // Yükleniyor durumuna geç
            val currentUserUid = auth.currentUser?.uid

            if (currentUserUid == null) {
                // HATA YÖNETİMİ 1: Kullanıcı girişi yoksa
                _uiState.value = ProfileUiState.Error("Kullanıcı oturumu bulunamadı.")
                return@launch
            }

            usersCollection.document(currentUserUid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Firestore dökümanını UserProfile nesnesine dönüştür
                        val profile = document.toObject(UserProfile::class.java)
                        if (profile != null) {
                            _uiState.value = ProfileUiState.Success(profile)
                        } else {
                            // HATA YÖNETİMİ 2: Döküman var ama nesneye çevrilemedi
                            _uiState.value = ProfileUiState.Error("Profil verileri okunamadı.")
                        }
                    } else {
                        // HATA YÖNETİMİ 3: Döküman bulunamadı
                        _uiState.value = ProfileUiState.Error("Kullanıcı profili veritabanında mevcut değil.")
                    }
                }
                .addOnFailureListener { exception ->
                    // HATA YÖNETİMİ 4: Firestore'a erişilemedi
                    _uiState.value = ProfileUiState.Error(exception.localizedMessage ?: "Veritabanına ulaşılamadı.")
                }
        }
    }

    // YENİ EKLENEN GÜNCELLEME FONKSİYONU
    fun updateUserProfile(
        newBio: String,
        newInterests: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            onError("Güncelleme yapılamadı: Oturum kapalı.")
            return
        }

        // Firestore'da sadece belirli alanları güncellemek için bir Map oluşturuyoruz.
        // Bu, "copy()" metodundan daha verimlidir çünkü tüm dökümanı yeniden yazmaz.
        val updates = mapOf(
            "bio" to newBio,
            "interests" to newInterests
        )

        usersCollection.document(currentUserUid).update(updates)
            .addOnSuccessListener {
                // Başarılı olursa, lokal state'i de güncelleyip UI'a haber ver
                fetchCurrentUserProfile() // En temiz yol, veriyi yeniden çekmektir.
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Güncelleme sırasında bir hata oluştu.")
            }
    }
}
