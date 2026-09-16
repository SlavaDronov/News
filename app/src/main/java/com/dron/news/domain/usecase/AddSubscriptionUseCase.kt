package com.dron.news.domain.usecase

import com.dron.news.domain.repository.NewsRepository
import com.dron.news.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddSubscriptionUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(topic: String) {
        // 1. Добавляем подписку
        newsRepository.addSubscription(topic)

        // 2. Получаем настройки (язык)
        val settings = settingsRepository.getSettings().first()

        // 3. Загружаем статьи для темы
        newsRepository.updateArticlesForTopic(topic, settings.language)
    }
}