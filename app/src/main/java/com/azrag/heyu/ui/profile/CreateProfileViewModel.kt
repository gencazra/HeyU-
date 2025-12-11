//----- CreateProfileViewModel.kt (FAKÜLTE/BÖLÜM İÇİN YENİ HALİ) -----

package com.azrag.heyu.ui.profile

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
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

class CreateProfileViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    // --- Data Kaynakları ---
    val allFaculties: List<String> = YeditepeData.faculties
    val allInterests: List<String> = listOf("Spor", "Müzik", "Sanat", "Teknoloji", "Oyun", "Sinema", "Gezi", "Yemek")

    // --- Arayüz State'leri ---
    val fullName: String = savedStateHandle.get<String>("fullName") ?: ""

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _faculty = MutableStateFlow("")
    val faculty: StateFlow<String> = _faculty.asStateFlow()

    private val _major = MutableStateFlow("")
    val major: StateFlow<String> = _major.asStateFlow()

    private val _majorsForSelectedFaculty = MutableStateFlow<List<String>>(emptyList())
    val majorsForSelectedFaculty: StateFlow<List<String>> = _majorsForSelectedFaculty.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _selectedInterests = MutableStateFlow<Set<String>>(emptySet())
    val selectedInterests: StateFlow<Set<String>> = _selectedInterests.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    // --- Arayüz Event'leri ---
    fun onPhotoSelected(uri: Uri?) {
        _photoUri.value = uri
    }

    fun onFacultySelected(selectedFaculty: String) {
        _faculty.value = selectedFaculty
        _majorsForSelectedFaculty.value = YeditepeData.getMajorsForFaculty(selectedFaculty)
        _major.value = "" // Fakülte değişince bölümü sıfırla
    }

    fun onMajorSelected(selectedMajor: String) {
        _major.value = selectedMajor
    }

    fun onBioChange(newBio: String) {
        _bio.value = newBio
    }

    fun onInterestChipClick(interest: String) {
        val currentSelection = _selectedInterests.value.toMutableSet()
        if (currentSelection.contains(interest)) {
            currentSelection.remove(interest)
        } else {
            currentSelection.add(interest)
        }
        _selectedInterests.value = currentSelection
    }

    fun saveProfile() {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val userUid = auth.currentUser?.uid
            if (userUid == null) {
                _saveState.value = SaveState.Error("Kullanıcı bulunamadı.")
                return@launch
            }

            try {
                // Fotoğrafı yükle ve URL'sini al
                var uploadedPhotoUrl: String? = null
                if (_photoUri.value != null) {
                    val photoRef = storage.reference.child("profile_pictures/$userUid")
                    val uploadTask = photoRef.putFile(_photoUri.value!!).await()
                    uploadedPhotoUrl = uploadTask.storage.downloadUrl.await().toString()
                }

                // Profil verilerini oluştur
                val profileData = UserProfile(
                    fullName = fullName,
                    photoUrl = uploadedPhotoUrl,
                    faculty = _faculty.value, // Güncelledik
                    major = _major.value,     // Güncelledik
                    bio = _bio.value,
                    interests = _selectedInterests.value.toList()
                )

                // Firestore'a kaydet
                firestore.collection("users").document(userUid).set(profileData).await()
                _saveState.value = SaveState.Success

            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Profil kaydedilemedi.")
            }
        }
    }
}
