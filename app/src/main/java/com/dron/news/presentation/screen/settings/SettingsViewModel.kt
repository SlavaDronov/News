package com.dron.news.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dron.news.domain.entity.Interval
import com.dron.news.domain.entity.Language
import com.dron.news.domain.entity.Settings
import com.dron.news.domain.usecase.ClearAllArticlesUseCase
import com.dron.news.domain.usecase.GetSettingsUseCase
import com.dron.news.domain.usecase.RestartRefreshWorkerUseCase   // ← НОВЫЙ
import com.dron.news.domain.usecase.UpdateIntervalUseCase
import com.dron.news.domain.usecase.UpdateLanguageUseCase
import com.dron.news.domain.usecase.UpdateNotificationsEnabledUseCase
import com.dron.news.domain.usecase.UpdateSubscribedArticlesUseCase
import com.dron.news.domain.usecase.UpdateWifiOnlyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val updateIntervalUseCase: UpdateIntervalUseCase,
    private val updateNotificationsEnabledUseCase: UpdateNotificationsEnabledUseCase,
    private val updateWifiOnlyUseCase: UpdateWifiOnlyUseCase,
    private val restartRefreshWorkerUseCase: RestartRefreshWorkerUseCase,
    private val clearAllArticlesUseCase: ClearAllArticlesUseCase,          // ← НОВЫЙ
    private val updateSubscribedArticlesUseCase: UpdateSubscribedArticlesUseCase
) : ViewModel() {

    val settings: StateFlow<Settings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings.DEFAULT
        )

    /**
     * Язык: очищаем БД + сразу загружаем новые статьи
     * Worker НЕ перезапускаем — он сам читает настройки
     */
    fun setLanguage(language: Language) {
        viewModelScope.launch {
            updateLanguageUseCase(language)
            clearAllArticlesUseCase()              // ← ОЧИСТИТЬ
            updateSubscribedArticlesUseCase()      // ← ЗАГРУЗИТЬ
        }
    }

    /**
     * Интервал: меняется расписание → перезапуск Worker ОБЯЗАТЕЛЕН
     */
    fun setInterval(interval: Interval) {
        viewModelScope.launch {
            updateIntervalUseCase(interval)
            restartRefreshWorkerUseCase()
        }
    }

    /**
     * Уведомления: ничего перезапускать не нужно
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateNotificationsEnabledUseCase(enabled)
        }
    }

    /**
     * Wi-Fi: меняются constraints → перезапуск Worker ОБЯЗАТЕЛЕН
     */
    fun setWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            updateWifiOnlyUseCase(wifiOnly)
            restartRefreshWorkerUseCase()
        }
    }
}