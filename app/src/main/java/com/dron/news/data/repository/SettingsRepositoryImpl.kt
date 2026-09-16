package com.dron.news.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dron.news.data.mapper.toInterval
import com.dron.news.domain.entity.Interval
import com.dron.news.domain.entity.Language
import com.dron.news.domain.entity.Settings
import com.dron.news.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val languageKey = stringPreferencesKey("language")
    private val intervalKey = intPreferencesKey("interval")
    private val notificationEnabledKey = booleanPreferencesKey("notification_enabled")
    private val wifiOnlyKey = booleanPreferencesKey("wifi_only")

    override fun getSettings(): Flow<Settings> {
        return context.dataStore.data.map { preferences ->
            val default = Settings.DEFAULT  // ← один раз берём defaults

            Settings(
                language = Language.valueOf(
                    preferences[languageKey] ?: default.language.name
                ),
                interval = (preferences[intervalKey] ?: default.interval.minutes)
                    .toInterval(),
                notificationEnabled = preferences[notificationEnabledKey]
                    ?: default.notificationEnabled,
                wifiOnly = preferences[wifiOnlyKey]
                    ?: default.wifiOnly
            )
        }
    }

    override suspend fun updateLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
    }

    override suspend fun updateInterval(min: Int) {
        context.dataStore.edit { preferences ->
            preferences[intervalKey] = min
        }
    }

    override suspend fun updateNotificationsEnables(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationEnabledKey] = enabled
        }
    }

    override suspend fun updateWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[wifiOnlyKey] = wifiOnly
        }
    }
}