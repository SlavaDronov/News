package com.dron.news.domain.repository

import com.dron.news.domain.entity.Language
import com.dron.news.domain.entity.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>

    suspend fun updateLanguage(language: Language)

    suspend fun updateInterval(min: Int)

    suspend fun updateNotificationsEnables(enabled: Boolean)

    suspend fun updateWifiOnly(wifiOnly: Boolean)

}