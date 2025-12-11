// Dosya: ui/dashboard/notices/AddNoticeViewModel.kt
package com.azrag.heyu.ui.dashboard.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.Notice
import com.azrag.heyu.data.NoticeRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddNoticeUiState {
    object Idle : AddNoticeUiState()
    object Loading : AddNoticeUiState()
    data class Success(val noticeId: String) : AddNoticeUiState()
    data class Error(val message: String) : AddNoticeUiState()
}

@HiltViewModel
class AddNoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddNoticeUiState>(AddNoticeUiState.Idle)
    val uiState: StateFlow<AddNoticeUiState> = _uiState

    fun createNotice(title: String, description: String, category: String) {
        if (title.isBlank() || description.isBlank() || category.isBlank()) {
            _uiState.value = AddNoticeUiState.Error("Tüm alanlar doldurulmalıdır.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AddNoticeUiState.Loading
            try {
                val newNotice = Notice(title = title, description = description, category = category, date = Timestamp.now())
                val noticeId = noticeRepository.addNotice(newNotice)
                if (noticeId != null) {
                    _uiState.value = AddNoticeUiState.Success(noticeId)
                } else {
                    _uiState.value = AddNoticeUiState.Error("Duyuru oluşturulamadı.")
                }
            } catch (e: Exception) {
                _uiState.value = AddNoticeUiState.Error(e.message ?: "Bilinmeyen hata.")
            }
        }
    }
}
