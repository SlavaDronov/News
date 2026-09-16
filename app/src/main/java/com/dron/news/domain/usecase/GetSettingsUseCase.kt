package com.dron.news.domain.usecase

import com.dron.news.domain.repository.SettingsRepository
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingRepository: SettingsRepository
) {
    operator fun invoke() = settingRepository.getSettings()
}