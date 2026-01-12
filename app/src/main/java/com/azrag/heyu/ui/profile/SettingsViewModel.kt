package com.azrag.heyu.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.SettingRepository
import com.azrag.heyu.data.repository.ThemeSetting
import com.azrag.heyu.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Eklendi: Navigasyon veya Dialog için state
    private val _navigateToPrivacy = MutableStateFlow(false)
    val navigateToPrivacy = _navigateToPrivacy.asStateFlow()

    fun onThemeChanged(isDark: Boolean) {
        viewModelScope.launch {
            settingRepository.setTheme(if (isDark) ThemeSetting.DARK else ThemeSetting.LIGHT)
        }
    }

    fun logout() {
        settingRepository.logout()
    }

    // GÜNCELLENDİ: Artık dış link açmak yerine state değiştiriyor
    fun openPrivacyPolicy() {
        _navigateToPrivacy.value = true
    }

    fun resetNavigation() {
        _navigateToPrivacy.value = false
    }

    fun rateApp(context: Context) {
        val packageName = context.packageName
        try {
            val marketUri = android.net.Uri.parse("market://details?id=$packageName")
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, marketUri))
        } catch (e: Exception) {
            val playStoreUri = android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, playStoreUri))
        }
    }
}
