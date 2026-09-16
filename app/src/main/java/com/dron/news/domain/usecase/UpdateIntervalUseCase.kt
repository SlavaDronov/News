package com.dron.news.domain.usecase

import com.dron.news.domain.entity.Interval
import com.dron.news.domain.repository.SettingsRepository

class UpdateIntervalUseCase(
    private val settingRepository: SettingsRepository
) {
    suspend fun invoke(interval: Interval){
        settingRepository.updateInterval(interval.minutes)
    }
}