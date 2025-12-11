//----- EditProfileViewModel.kt (FOTOĞRAF VE SINIF SEVİYESİ EKLENMİŞ SON HALİ) -----

package com.azrag.heyu.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.YeditepeData
import com.azrag.heyu.ui.dashboard.discover.UserProfile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val currentUserUid = auth.currentUser?.uid

    // --- Data Kaynakları ---
    val allFaculties: List<String> = YeditepeData.faculties
    val allClassLevels: List<String> = listOf("Hazırlık", "1. Sınıf", "2. Sınıf", "3. Sınıf", "4. Sınıf", "5. Sınıf", "Yüksek Lisans", "Doktora", "Mezun")
    val allInterests: List<String> = listOf("Spor", "Müzik", "Sanat", "Teknoloji", "Oyun", "Sinema", "Gezi", "Yemek")

    // --- Arayüz State'leri ---
    private val _currentPhotoUrl = MutableStateFlow<String?>(null)
    val currentPhotoUrl: StateFlow<String?> = _currentPhotoUrl.asStateFlow()

    private val _newPhotoUri = MutableStateFlow<Uri?>(null)
    val newPhotoUri: StateFlow<Uri?> = _newPhotoUri.asStateFlow()

    private val _faculty = MutableStateFlow("")
    val faculty: StateFlow<String> = _faculty.asStateFlow()

    private val _major = MutableStateFlow("")
    val major: StateFlow<String> = _major.asStateFlow()

    private val _majorsForSelectedFaculty = MutableStateFlow<List<String>>(emptyList())
    val majorsForSelectedFaculty: StateFlow<List<String>> = _majorsForSelectedFaculty.asStateFlow()

    private val _classLevel = MutableStateFlow("") // YENİ: Sınıf seviyesi state'i
    val classLevel: StateFlow<String> = _classLevel.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _selectedInterests = MutableStateFlow<Set<String>>(emptySet())
    val selectedInterests: StateFlow<Set<String>> = _selectedInterests.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        fetchCurrentUserProfile()
    }

    private fun fetchCurrentUserProfile() {
        if (currentUserUid == null) return

        firestore.collection("users").document(currentUserUid)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java) ?: return@addOnSuccessListener

                _currentPhotoUrl.value = profile.photoUrl
                _faculty.value = profile.faculty ?: ""
                _major.value = profile.major ?: ""
                _classLevel.value = profile.classLevel ?: "" // YENİ: Veriyi state'e ata
                _bio.value = profile.bio ?: ""
                _selectedInterests.value = profile.interests?.toSet() ?: emptySet()

                if (profile.faculty != null) {
                    _majorsForSelectedFaculty.value = YeditepeData.getMajorsForFaculty(profile.faculty)
                }
            }
    }

    // --- Arayüz Event'leri ---
    fun onPhotoSelected(uri: Uri?) { _newPhotoUri.value = uri }
    fun onFacultySelected(selectedFaculty: String) {
        _faculty.value = selectedFaculty
        _majorsForSelectedFaculty.value = YeditepeData.getMajorsForFaculty(selectedFaculty)
        _major.value = ""
    }
    fun onMajorSelected(selectedMajor: String) { _major.value = selectedMajor }
    fun onClassLevelSelected(level: String) { _classLevel.value = level } // YENİ
    fun onBioChange(newBio: String) { _bio.value = newBio }
    fun onInterestChipClick(interest: String) {
        val currentSelection = _selectedInterests.value.toMutableSet()
        if (currentSelection.contains(interest)) { currentSelection.remove(interest) }
        else { currentSelection.add(interest) }
        _selectedInterests.value = currentSelection
    }

    fun saveProfileChanges() {
        if (currentUserUid == null) {
            _saveState.value = SaveState.Error("Kullanıcı oturumu bulunamadı.")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                var photoToSave = _currentPhotoUrl.value
                if (_newPhotoUri.value != null) {
                    val photoRef = storage.reference.child("profile_pictures/$currentUserUid")
                    val uploadTask = photoRef.putFile(_newPhotoUri.value!!).await()
                    photoToSave = uploadTask.storage.downloadUrl.await().toString()
                }

                val updatedData = mutableMapOf<String, Any>(
                    "faculty" to _faculty.value,
                    "major" to _major.value,
                    "classLevel" to _classLevel.value, // YENİ: Kaydedilecek veriye ekle
                    "bio" to _bio.value,
                    "interests" to _selectedInterests.value.toList()
                )
                if (photoToSave != null) {
                    updatedData["photoUrl"] = photoToSave
                }

                firestore.collection("users").document(currentUserUid)
                    .update(updatedData)
                    .await()

                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Profil güncellenemedi.")
            }
        }
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

