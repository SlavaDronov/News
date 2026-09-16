package com.dron.news.domain.usecase

import com.dron.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ClearAllArticlesUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke() {
        val topics = newsRepository.getAllSubscriptions().first()
        newsRepository.clearAllArticles(topics)
    }
}