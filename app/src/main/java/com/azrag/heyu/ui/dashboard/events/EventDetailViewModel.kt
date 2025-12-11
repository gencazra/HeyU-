// Dosya: ui/dashboard/events/EventDetailViewModel.kt

package com.azrag.heyu.ui.dashboard.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.Event
import com.azrag.heyu.data.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Etkinlik detay sayfasının UI durumları
sealed class EventDetailUiState {
    object Loading : EventDetailUiState()
    data class Success(val event: Event) : EventDetailUiState()
    data class Error(val message: String) : EventDetailUiState()
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    // SavedStateHandle, navigasyon argümanlarına (örn: eventId) ViewModel içinden erişmemizi sağlar.
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState

    init {
        // Navigasyondan gelen "eventId" argümanını al ve veriyi çek
        savedStateHandle.get<String>("eventId")?.let {
            fetchEventDetails(it)
        } ?: run {
            _uiState.value = EventDetailUiState.Error("Etkinlik ID'si bulunamadı.")
        }
    }

    private fun fetchEventDetails(eventId: String) {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            try {
                val event = eventRepository.getEventById(eventId)
                if (event != null) {
                    _uiState.value = EventDetailUiState.Success(event)
                } else {
                    _uiState.value = EventDetailUiState.Error("Etkinlik bulunamadı.")
                }
            } catch (e: Exception) {
                _uiState.value = EventDetailUiState.Error(e.message ?: "Etkinlik detayı yüklenemedi.")
            }
        }
    }

    // Katıl/Ayrıl butonuna tıklandığında çalışır
    fun onJoinLeaveClick() {
        val currentState = _uiState.value
        if (currentState is EventDetailUiState.Success) {
            viewModelScope.launch {
                val eventId = currentState.event.id
                val success = eventRepository.toggleParticipation(eventId)
                if (success) {
                    // Katılım durumu değiştiğinde, arayüzün güncellenmesi için
                    // etkinlik verilerini yeniden çekiyoruz.
                    fetchEventDetails(eventId)
                }
                // Hata durumu UI'da bir Toast ile gösterilebilir.
            }
        }
    }

    // Ekran her açıldığında verinin yenilenmesi için
    fun refreshEventDetails() {
        savedStateHandle.get<String>("eventId")?.let {
            fetchEventDetails(it)
        }
    }
}
