package com.dron.news.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dron.news.domain.entity.Interval
import com.dron.news.domain.entity.Language
import com.dron.news.domain.entity.Settings
import com.dron.news.domain.usecase.ClearAllArticlesUseCase
import com.dron.news.domain.usecase.GetSettingsUseCase
import com.dron.news.domain.usecase.RestartRefreshWorkerUseCase
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
    private val clearAllArticlesUseCase: ClearAllArticlesUseCase,
    private val updateSubscribedArticlesUseCase: UpdateSubscribedArticlesUseCase
) : ViewModel() {

    val settings: StateFlow<Settings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings.DEFAULT
        )

    fun processCommand(command: SettingsCommand) {
        viewModelScope.launch {
            when (command) {
                is SettingsCommand.SelectLanguage -> {
                    updateLanguageUseCase(command.language)
                    clearAllArticlesUseCase()
                    updateSubscribedArticlesUseCase()
                }
                is SettingsCommand.SelectInterval -> {
                    updateIntervalUseCase(command.interval)
                    restartRefreshWorkerUseCase()
                }
                is SettingsCommand.SetNotificationsEnabled -> {
                    updateNotificationsEnabledUseCase(command.enabled)
                }
                is SettingsCommand.SetWifiOnly -> {
                    updateWifiOnlyUseCase(command.wifiOnly)
                    restartRefreshWorkerUseCase()
                }
            }
        }
    }
}

sealed interface SettingsCommand {
    data class SelectLanguage(val language: Language) : SettingsCommand
    data class SelectInterval(val interval: Interval) : SettingsCommand
    data class SetNotificationsEnabled(val enabled: Boolean) : SettingsCommand
    data class SetWifiOnly(val wifiOnly: Boolean) : SettingsCommand
}