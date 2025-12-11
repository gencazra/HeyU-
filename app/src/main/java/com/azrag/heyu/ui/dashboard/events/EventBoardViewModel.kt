// Dosya: ui/dashboard/events/EventBoardViewModel.kt

package com.azrag.heyu.ui.dashboard.events

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.Event
import com.azrag.heyu.data.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed class EventBoardUiState {
    object Loading : EventBoardUiState()
    data class Success(val events: List<Event>) : EventBoardUiState()
    data class Error(val message: String) : EventBoardUiState()
}

@HiltViewModel
class EventBoardViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventBoardUiState>(EventBoardUiState.Loading)
    val uiState: StateFlow<EventBoardUiState> = _uiState

    // Filtreleme için State'ler
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedChip = MutableStateFlow<String?>(null)
    val selectedChip = _selectedChip.asStateFlow()

    private var _allEvents = listOf<Event>() // Ham listeyi tutar

    init {
        fetchAndCombineEvents()
    }

    private fun fetchAndCombineEvents() {
        viewModelScope.launch {
            _uiState.value = EventBoardUiState.Loading
            try {
                // 1. Veritabanından tüm etkinlikleri bir kere çek ve sakla
                _allEvents = eventRepository.getAllEvents()

                // 2. Filtre state'leri her değiştiğinde bu blok yeniden çalışsın
                combine(_searchText, _selectedChip) { text, chip ->
                    var filteredEvents = _allEvents

                    // Arama metnine göre filtrele (başlık veya açıklamada)
                    if (text.isNotBlank()) {
                        filteredEvents = filteredEvents.filter {
                            it.title.contains(text, ignoreCase = true) ||
                                    it.description.contains(text, ignoreCase = true)
                        }
                    }

                    // Çiplere göre filtrele
                    chip?.let {
                        val today = Calendar.getInstance()
                        when (it) {
                            "Bugün" -> {
                                filteredEvents = filteredEvents.filter { event ->
                                    val eventCal = Calendar.getInstance().apply { time = event.date.toDate() }
                                    today.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                                            today.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)
                                }
                            }
                            "Bu Hafta" -> {
                                val startOfWeek = (today.clone() as Calendar).apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }
                                val endOfWeek = (startOfWeek.clone() as Calendar).apply { add(Calendar.DAY_OF_WEEK, 6) }
                                filteredEvents = filteredEvents.filter { event ->
                                    event.date.toDate().after(startOfWeek.time) && event.date.toDate().before(endOfWeek.time)
                                }
                            }
                            // Diğer kategoriler (senin projenin mantığına göre)
                            "Kulüpler" -> filteredEvents = filteredEvents.filter { e -> e.category.equals("Kulüp", true) }
                            "Spor" -> filteredEvents = filteredEvents.filter { e -> e.category.equals("Spor", true) }
                            "Müzik" -> filteredEvents = filteredEvents.filter { e -> e.category.equals("Müzik", true) }
                        }
                    }
                    _uiState.value = EventBoardUiState.Success(filteredEvents)
                }.collect {}

            } catch (e: Exception) {
                _uiState.value = EventBoardUiState.Error(e.message ?: "Etkinlikler yüklenemedi.")
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    fun onChipSelected(chip: String) {
        // Aynı çipe tekrar basılırsa filtreyi kaldır
        if (_selectedChip.value == chip) {
            _selectedChip.value = null
        } else {
            _selectedChip.value = chip
        }
    }

    // Yeni etkinlik eklendiğinde veya ekrandan geri dönüldüğünde listeyi yenile
    fun refreshEvents() {
        fetchAndCombineEvents()
    }
}
