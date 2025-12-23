package com.azrag.heyu.ui.dashboard.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.Notice
import com.azrag.heyu.data.repository.NoticeRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface NoticeBoardUiState {
    object Loading : NoticeBoardUiState
    data class Success(val notices: List<Notice>) : NoticeBoardUiState
    data class Error(val message: String) : NoticeBoardUiState
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
            when (val result = noticeRepository.getAllNotices()) {
                is Result.Success -> _uiState.value = NoticeBoardUiState.Success(result.data ?: emptyList())
                is Result.Error -> _uiState.value = NoticeBoardUiState.Error(result.message ?: "Hata oluştu.")
                else -> {}
            }
        }
    }

    fun onImInClicked(noticeId: String) {
        viewModelScope.launch {
            noticeRepository.toggleNoticeParticipation(noticeId)
            fetchNotices()
        }
    }
}


sealed interface AddNoticeUiState {
    object Idle : AddNoticeUiState
    object Loading : AddNoticeUiState
    object Success : AddNoticeUiState
    data class Error(val message: String) : AddNoticeUiState
}

@HiltViewModel
class AddNoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddNoticeUiState>(AddNoticeUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun createNotice(title: String, description: String, category: String) {
        if (title.isBlank() || description.isBlank() || category.isBlank()) {
            _uiState.value = AddNoticeUiState.Error("Tüm alanları doldurun.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddNoticeUiState.Loading

            when (val userResult = userRepository.getCurrentUserProfile()) {
                is Result.Success -> {
                    val user = userResult.data ?: return@launch
                    val newNotice = Notice(
                        creatorId = user.id,
                        creatorName = user.displayName ?: "İsimsiz",
                        creatorImageUrl = user.photoUrl ?: "",
                        title = title,
                        description = description,
                        category = category
                    )

                    when (noticeRepository.addNotice(newNotice)) {
                        is Result.Success -> _uiState.value = AddNoticeUiState.Success
                        is Result.Error -> _uiState.value = AddNoticeUiState.Error("Duyuru eklenemedi.")
                        else -> {}
                    }
                }
                is Result.Error -> {
                    _uiState.value = AddNoticeUiState.Error("Profil bilgileri alınamadı.")
                }
                else -> {}
            }
        }
    }

    fun onUiStateHandled() {
        _uiState.value = AddNoticeUiState.Idle
    }
}
