package com.azrag.heyu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.SettingRepository
import com.azrag.heyu.data.repository.ThemeSetting
import com.azrag.heyu.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = userRepository.getCurrentUserProfileStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeSetting: StateFlow<ThemeSetting> = settingRepository.themeSetting
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeSetting.SYSTEM)

    fun onThemeChanged(isDark: Boolean) {
        viewModelScope.launch {
            settingRepository.setTheme(if (isDark) ThemeSetting.DARK else ThemeSetting.LIGHT)
        }
    }

    fun logout() {
        settingRepository.logout()
    }
}
