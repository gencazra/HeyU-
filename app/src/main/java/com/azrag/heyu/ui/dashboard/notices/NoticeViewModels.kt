package com.azrag.heyu.ui.dashboard.notices

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.Notice
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.NoticeRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class NoticeUiState(
    val isLoadingList: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val notices: List<Notice> = emptyList(),
    val selectedNotice: Notice? = null,
    val participants: List<UserProfile> = emptyList(),
    val listError: String? = null,
    val detailError: String? = null
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchNotices()
    }

    fun fetchNotices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true) }
            when (val result = noticeRepository.getAllNotices()) {
                is Result.Success -> _uiState.update { it.copy(isLoadingList = false, notices = result.data ?: emptyList()) }
                is Result.Error -> _uiState.update { it.copy(isLoadingList = false, listError = result.message) }
                else -> _uiState.update { it.copy(isLoadingList = false) }
            }
        }
    }

    fun loadNoticeDetails(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            when (val result = noticeRepository.getNoticeById(id)) {
                is Result.Success -> {
                    val notice = result.data
                    _uiState.update { it.copy(isLoadingDetail = false, selectedNotice = notice) }
                    loadParticipantProfiles(notice?.attendees ?: emptyList())
                }
                is Result.Error -> _uiState.update { it.copy(isLoadingDetail = false, detailError = result.message) }
                else -> _uiState.update { it.copy(isLoadingDetail = false) }
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

    fun onImInClicked(noticeId: String) {
        viewModelScope.launch {
            val result = noticeRepository.toggleNoticeParticipation(noticeId)
            if (result is Result.Success) {
                fetchNotices()
                if (_uiState.value.selectedNotice?.id == noticeId) {
                    loadNoticeDetails(noticeId)
                }
            }
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

    var eventDate = mutableStateOf("")
    var eventTime = mutableStateOf("")
    var location = mutableStateOf("")
    var imageUrl = mutableStateOf("")

    fun createNotice(title: String, description: String, category: String) {
        if (title.isBlank() || description.isBlank() || category.isBlank()) {
            _uiState.value = AddNoticeUiState.Error("Lütfen gerekli alanları doldurun.")
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
                        category = category,
                        eventDate = if (eventDate.value.isNotBlank()) eventDate.value else null,
                        eventTime = if (eventTime.value.isNotBlank()) eventTime.value else null,
                        location = if (location.value.isNotBlank()) location.value else null,
                        imageUrl = if (imageUrl.value.isNotBlank()) imageUrl.value else null
                    )

                    val addResult = noticeRepository.addNotice(newNotice)
                    if (addResult is Result.Success) {
                        _uiState.value = AddNoticeUiState.Success
                    } else if (addResult is Result.Error) {
                        _uiState.value = AddNoticeUiState.Error(addResult.message ?: "Paylaşım yapılamadı.")
                    }
                }
                is Result.Error -> {
                    _uiState.value = AddNoticeUiState.Error("Profil bilgileri alınamadı.")
                }
                else -> {}
            }
        }
    }

    fun onDateChange(y: Int, m: Int, d: Int) {
        eventDate.value = "$d/${m + 1}/$y"
    }

    fun onTimeChange(h: Int, min: Int) {
        eventTime.value = String.format("%02d:%02d", h, min)
    }

    fun onUiStateHandled() {
        _uiState.value = AddNoticeUiState.Idle
    }
}
