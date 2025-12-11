package com.azrag.heyu.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// data class UserProfile aynı kalabilir, iyi tasarlanmış.
data class UserProfile(
    val userId: String = "",
    val fullName: String = "",
    val department: String = "",
    val bio: String = "", // Not: Null yerine boş string kullanmak null kontrollerini azaltır.
    val interests: List<String> = emptyList(),
    val avatarUrl: String = ""
)

// UI State sınıfı da aynı kalabilir, bu modern ve doğru bir yaklaşım.
sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val userProfile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

// İYİLEŞTİRME: Profili Düzenleme ekranının state'ini kendi data class'ında toplamak
// Bu, ViewModel içinde dağınık 'var'lar olmasını engeller ve state yönetimini merkezileştirir.
data class EditProfileState(
    val bio: String = "",
    val selectedInterests: Set<String> = emptySet(),
    val isSaving: Boolean = false // Kaydetme butonunda loading göstermek için
)

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Profil GÖRÜNTÜLEME ekranı için StateFlow
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // Profil DÜZENLEME ekranı için StateFlow
    private val _editState = MutableStateFlow(EditProfileState())
    val editState = _editState.asStateFlow()

    init {
        // ViewModel oluşturulduğunda mevcut kullanıcı için verileri yükle
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = ProfileUiState.Error("Kullanıcı oturumu bulunamadı.")
                return@launch
            }

            try {
                val snapshot = firestore.collection("users").document(userId).get().await()
                // İYİLEŞTİRME: toObject<>() null dönebilir, güvenli kontrol ekledim.
                snapshot.toObject<UserProfile>()?.let { user ->
                    _uiState.value = ProfileUiState.Success(user)
                    // Düzenleme ekranı için state'leri de başlangıç verileriyle doldur
                    _editState.update { currentState ->
                        currentState.copy(
                            bio = user.bio,
                            selectedInterests = user.interests.toSet()
                        )
                    }
                } ?: run {
                    _uiState.value = ProfileUiState.Error("Profil bilgileri bulunamadı.")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Veri alınırken hata oluştu.")
            }
        }
    }

    // --- Düzenleme Ekranı İçin Fonksiyonlar ---

    fun onBioChange(newBio: String) {
        _editState.update { it.copy(bio = newBio) }
    }

    fun onInterestChange(interest: String, isSelected: Boolean) {
        _editState.update { currentState ->
            val newInterests = currentState.selectedInterests.toMutableSet()
            if (isSelected) {
                newInterests.add(interest)
            } else {
                newInterests.remove(interest)
            }
            currentState.copy(selectedInterests = newInterests)
        }
    }

    fun saveChanges(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                onError("Güncelleme için kullanıcı bulunamadı.")
                return@launch
            }

            // Kaydetme işlemi başlarken loading durumunu aktif et
            _editState.update { it.copy(isSaving = true) }

            val currentState = _editState.value
            val updatedData = mapOf(
                "bio" to currentState.bio,
                "interests" to currentState.selectedInterests.toList()
            )

            try {
                firestore.collection("users").document(userId).update(updatedData).await()
                fetchUserProfile() // Lokal veriyi ve ana profil ekranını tazelemek için profili yeniden çek
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Profil güncellenirken bir hata oluştu.")
            } finally {
                // İşlem başarılı da olsa, hata da alsa loading durumunu bitir
                _editState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
