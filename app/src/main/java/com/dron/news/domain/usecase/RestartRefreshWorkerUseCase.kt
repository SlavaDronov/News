package com.dron.news.domain.usecase

import com.dron.news.domain.repository.NewsRepository
import com.dron.news.domain.repository.SettingsRepository
import com.dron.news.data.mapper.toRefreshConfig
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RestartRefreshWorkerUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke() {
        // 1. Читаем актуальные настройки
        val settings = settingsRepository.getSettings().first()

        // 2. Преобразуем в RefreshConfig
        val config = settings.toRefreshConfig()

        // 3. Перезапускаем Worker с новым конфигом
        newsRepository.startBackgroundRefresh(config)
    }
}