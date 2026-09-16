package com.dron.news.domain.usecase

import com.dron.news.domain.entity.Article
import com.dron.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClearAllArticlesUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(topics: List<String>) {
        newsRepository.clearAllArticles(topics)
    }
}