package com.dron.news.domain.usecase

import com.dron.news.domain.entity.Article
import com.dron.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArticlesByTopicsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(topics: List<String>): Flow<List<Article>> {
        return newsRepository.getArticlesByTopics(topics)
    }
}