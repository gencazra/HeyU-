package com.azrag.heyu.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

@Singleton
class SettingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) {

    private object PreferencesKeys {
        val THEME_SETTING = stringPreferencesKey("theme_setting")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val themeSetting: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_SETTING] ?: ThemeSetting.SYSTEM.name
            ThemeSetting.valueOf(themeName)
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    val selectedLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "en"
        }

    suspend fun setTheme(theme: ThemeSetting) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.THEME_SETTING] = theme.name
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LANGUAGE] = languageCode
        }
    }

    fun logout() {
        auth.signOut()
    }
}
