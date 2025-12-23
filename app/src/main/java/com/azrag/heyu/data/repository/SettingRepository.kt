
package com.azrag.heyu.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
class SettingRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val THEME_SETTING = stringPreferencesKey("theme_setting")
    }


    val themeSetting: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_SETTING] ?: ThemeSetting.SYSTEM.name
            ThemeSetting.valueOf(themeName)
        }

    suspend fun setTheme(theme: ThemeSetting) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.THEME_SETTING] = theme.name
        }
    }
}
