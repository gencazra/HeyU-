package com.azrag.heyu.ui.dashboard.events

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.Event
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.EventRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.ModerationManager
import com.azrag.heyu.util.Result
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * heyU! - Etkinlik Yönetimi ViewModel
 * Admin kısıtlamaları, Katılımcı yönetimi ve Moderasyon mühürlenmiştir.
 */
data class EventsUiState(
    val isLoadingList: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val events: List<Event> = emptyList(),
    val selectedEvent: Event? = null,
    val participants: List<UserProfile> = emptyList(),
    val isCurrentUserAdmin: Boolean = false,
    val currentUserProfile: UserProfile? = null,
    val detailError: String? = null
)

sealed class UiEvent {
    object SaveSuccess : UiEvent()
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState = _uiState.asStateFlow()

    // Etkinlik Oluşturma Form State'leri
    var title = mutableStateOf("")
    var description = mutableStateOf("")
    var organizer = mutableStateOf("")
    var location = mutableStateOf("")
    var imageUrl = mutableStateOf("")
    var dateText = mutableStateOf("")
    var timeText = mutableStateOf("")
    var isSaving = mutableStateOf(false)
    var formError = mutableStateOf<String?>(null)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadEvents()
        fetchCurrentProfileAndAdmin()
    }

    /**
     * Kullanıcının Admin olup olmadığını kontrol eder.
     */
    private fun fetchCurrentProfileAndAdmin() {
        viewModelScope.launch {
            val res = userRepository.getCurrentUserProfile()
            if (res is Result.Success) {
                _uiState.update { it.copy(
                    isCurrentUserAdmin = res.data?.isAdmin ?: false,
                    currentUserProfile = res.data
                ) }
            }
        }
    }

    /**
     * Tüm etkinlikleri listeler.
     */
    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true) }
            when (val res = eventRepository.getAllEvents()) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoadingList = false,
                        events = res.data ?: emptyList()
                    ) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingList = false) }
                }
                else -> {
                    _uiState.update { it.copy(isLoadingList = false) }
                }
            }
        }
    }

    /**
     * Yeni etkinlik ekler (Sadece Admin yetkisi ve Temiz içerik ile).
     */
    fun onSaveClick() {
        // 1. Admin Kontrolü
        if (!_uiState.value.isCurrentUserAdmin) {
            formError.value = "Etkinlik paylaşmak için Admin yetkisi gerekir."
            return
        }

        // 2. Moderasyon Botu Kontrolü (Küfür/Cinsel İçerik)
        if (!ModerationManager.isSafe(title.value) || !ModerationManager.isSafe(description.value)) {
            formError.value = "Uygunsuz içerik tespit edildi! Lütfen topluluk kurallarına uyun."
            return
        }

        // 3. Boş Alan Kontrolü
        if (title.value.isBlank() || description.value.isBlank()) {
            formError.value = "Lütfen başlık ve açıklama alanlarını doldurun."
            return
        }

        viewModelScope.launch {
            isSaving.value = true
            val event = Event(
                title = title.value,
                description = ModerationManager.filterText(description.value), // Filtrelenmiş metin
                organizer = organizer.value,
                creatorName = _uiState.value.currentUserProfile?.displayName ?: "Admin",
                location = location.value,
                imageUrl = imageUrl.value,
                eventDate = dateText.value,
                eventTime = timeText.value,
                creatorId = Firebase.auth.currentUser?.uid ?: ""
            )

            val res = eventRepository.addEvent(event)
            isSaving.value = false

            if (res is Result.Success) {
                _eventFlow.emit(UiEvent.SaveSuccess)
                loadEvents() // Listeyi yenile
            } else if (res is Result.Error) {
                formError.value = res.message
            }
        }
    }

    /**
     * Etkinliğe katılma veya ayrılma işlemi.
     */
    fun onJoinLeaveClick(id: String) {
        viewModelScope.launch {
            val result = eventRepository.toggleUserAttendance(id)
            if (result is Result.Success) {
                // Katılım sonrası hem listeyi hem detayı yenile
                loadEventDetails(id)
                loadEvents()
            }
        }
    }

    fun loadEventDetails(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            when (val res = eventRepository.getEventById(id)) {
                is Result.Success -> {
                    val event = res.data
                    _uiState.update { it.copy(
                        isLoadingDetail = false,
                        selectedEvent = event
                    ) }
                    // Katılımcıların profillerini yükle
                    loadParticipantProfiles(event?.participants ?: emptyList())
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingDetail = false, detailError = res.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoadingDetail = false) }
                }
            }
        }
    }

    private fun loadParticipantProfiles(uids: List<String>) {
        viewModelScope.launch {
            val profiles = mutableListOf<UserProfile>()
            uids.forEach { uid ->
                val res = userRepository.getUserProfile(uid)
                if (res is Result.Success && res.data != null) {
                    profiles.add(res.data)
                }
            }
            _uiState.update { it.copy(participants = profiles) }
        }
    }

    fun onDateChange(y: Int, m: Int, d: Int) {
        dateText.value = "$d/${m + 1}/$y"
    }

    fun onTimeChange(h: Int, min: Int) {
        timeText.value = String.format("%02d:%02d", h, min)
    }
}
