package com.azrag.heyu.ui.signup

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.YeditepeData
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {


    var age = mutableStateOf("")
    var selectedFaculty = mutableStateOf("")
    var major = mutableStateOf("")
    var classLevel = mutableStateOf("")
    var selectedHobbies = mutableStateOf<List<String>>(emptyList())
    var bio = mutableStateOf("")
    var imageUri = mutableStateOf<Uri?>(null)

    var isLoading = mutableStateOf(false)
    var error = mutableStateOf<String?>(null)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        object NavigateToMajor : UiEvent()
        object NavigateToHobbies : UiEvent()
        object NavigateToPicture : UiEvent()
        object OnboardingComplete : UiEvent()
    }

    fun onFacultySelected(faculty: String) {
        selectedFaculty.value = faculty
        major.value = ""
    }

    fun onNameAgeNextClicked() {
        if (age.value.isBlank()) {
            error.value = "Yaş gerekli"
        } else {
            viewModelScope.launch { _eventFlow.emit(UiEvent.NavigateToMajor) }
        }
    }

    fun onMajorNextClicked() {
        if (major.value.isBlank() || classLevel.value.isBlank()) {
            error.value = "Lütfen bölüm ve sınıf seçiniz."
        } else {
            viewModelScope.launch { _eventFlow.emit(UiEvent.NavigateToHobbies) }
        }
    }

    fun onHobbiesBioNextClicked() {
        if (selectedHobbies.value.isEmpty()) {
            error.value = "En az bir hobi seçin"
        } else {
            viewModelScope.launch { _eventFlow.emit(UiEvent.NavigateToPicture) }
        }
    }

    fun onCompleteClicked() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            error.value = "Oturum bulunamadı."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            val profile = UserProfile(
                id = user.uid,
                displayName = user.displayName ?: "Öğrenci",
                age = age.value.toIntOrNull() ?: 0,
                department = "${major.value} (${classLevel.value}. Sınıf)",
                hobbies = selectedHobbies.value,
                bio = bio.value.ifBlank { "HeyU kullanıcısı!" }
            )

            val result: Result<Unit> = userRepository.saveUserProfile(profile, imageUri.value)

            when (result) {
                is Result.Success<Unit> -> {
                    _eventFlow.emit(UiEvent.OnboardingComplete)
                }
                is Result.Error -> {
                    error.value = result.message
                }
                is Result.Loading -> {
                }
            }
            isLoading.value = false
        }
    }

    fun onHobbyClicked(hobby: String) {
        val current = selectedHobbies.value.toMutableList()
        if (current.contains(hobby)) {
            current.remove(hobby)
        } else if (current.size < 5) {
            current.add(hobby)
        } else {
            error.value = "En fazla 5 hobi seçebilirsiniz."
        }
        selectedHobbies.value = current
    }
}
