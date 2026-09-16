package com.dron.news.domain.usecase

import com.dron.news.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateWifiOnlyUseCase @Inject constructor(
    private val settingRepository: SettingsRepository
) {
    suspend fun invoke(wifiOnly: Boolean){
        settingRepository.updateWifiOnly(wifiOnly)
    }
}