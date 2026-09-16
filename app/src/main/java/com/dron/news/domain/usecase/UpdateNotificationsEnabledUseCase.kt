package com.dron.news.domain.usecase

import com.dron.news.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateNotificationsEnabledUseCase @Inject constructor(
    private val settingRepository: SettingsRepository
) {
    suspend fun invoke(enabled: Boolean){
        settingRepository.updateNotificationsEnables(enabled)
    }
}