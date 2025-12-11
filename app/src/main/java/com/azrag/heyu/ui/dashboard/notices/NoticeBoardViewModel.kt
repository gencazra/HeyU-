// Dosya: ui/dashboard/notices/NoticeBoardViewModel.kt
package com.azrag.heyu.ui.dashboard.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.Notice
import com.azrag.heyu.data.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NoticeBoardUiState {
    object Loading : NoticeBoardUiState()
    data class Success(val notices: List<Notice>) : NoticeBoardUiState()
    data class Error(val message: String) : NoticeBoardUiState()
}

@HiltViewModel
class NoticeBoardViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoticeBoardUiState>(NoticeBoardUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchNotices()
    }

    fun fetchNotices() {
        viewModelScope.launch {
            _uiState.value = NoticeBoardUiState.Loading
            try {
                _uiState.value = NoticeBoardUiState.Success(noticeRepository.getAllNotices())
            } catch (e: Exception) {
                _uiState.value = NoticeBoardUiState.Error(e.message ?: "Duyurular yüklenemedi.")
            }
        }
    }

    fun onImInClicked(noticeId: String) {
        viewModelScope.launch {
            val success = noticeRepository.toggleNoticeParticipation(noticeId)
            if (success) {
                // Listeyi anlık güncellemek için verileri yeniden çek
                fetchNotices()
            }
        }
    }
}
