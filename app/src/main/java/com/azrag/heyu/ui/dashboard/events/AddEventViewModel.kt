// Dosya: ui/dashboard/events/AddEventViewModel.kt

package com.azrag.heyu.ui.dashboard.events

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.Event
import com.azrag.heyu.data.EventRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Arayüzün olası durumlarını temsil eder
sealed class AddEventUiState {
    object Idle : AddEventUiState() // Başlangıç, bekleme durumu
    object Loading : AddEventUiState() // Etkinlik kaydediliyor
    data class Success(val eventId: String) : AddEventUiState() // Başarıyla kaydedildi
    data class Error(val message: String) : AddEventUiState() // Hata oluştu
}

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEventUiState>(AddEventUiState.Idle)
    val uiState: StateFlow<AddEventUiState> = _uiState

    fun createEvent(
        title: String,
        description: String,
        location: String,
        category: String,
        date: Timestamp,
        imageUri: Uri? // Seçilen görselin URI'ı
    ) {
        if (title.isBlank() || description.isBlank() || location.isBlank() || category.isBlank()) {
            _uiState.value = AddEventUiState.Error("Lütfen tüm zorunlu alanları doldurun.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddEventUiState.Loading

            try {
                var imageUrl = ""
                // Eğer kullanıcı bir görsel seçtiyse, önce onu Storage'a yükle
                if (imageUri != null) {
                    imageUrl = eventRepository.uploadEventImage(imageUri) ?: ""
                }

                val newEvent = Event(
                    title = title,
                    description = description,
                    location = location,
                    category = category,
                    date = date,
                    imageUrl = imageUrl // Yüklenen görselin URL'si
                )

                val createdEventId = eventRepository.addEvent(newEvent)
                if (createdEventId != null) {
                    _uiState.value = AddEventUiState.Success(createdEventId)
                } else {
                    _uiState.value = AddEventUiState.Error("Etkinlik oluşturulamadı.")
                }
            } catch (e: Exception) {
                _uiState.value = AddEventUiState.Error(e.message ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }
}
