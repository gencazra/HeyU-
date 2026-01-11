package com.azrag.heyu.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
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

    fun openPrivacyPolicy(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://heyu.app/privacy"))
        context.startActivity(intent)
    }

    fun rateApp(context: Context) {
        val packageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            settingRepository.setLanguage(languageCode)
        }
    }
}
