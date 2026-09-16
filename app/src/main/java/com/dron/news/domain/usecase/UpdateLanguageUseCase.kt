package com.dron.news.domain.usecase

import com.dron.news.domain.entity.Language
import com.dron.news.domain.repository.SettingsRepository

class UpdateLanguageUseCase(
    private val settingRepository: SettingsRepository
) {
    suspend fun invoke(language: Language){
        settingRepository.updateLanguage(language)
    }
}