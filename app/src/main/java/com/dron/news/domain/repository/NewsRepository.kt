package com.dron.news.domain.repository

import com.dron.news.domain.entity.Article
import com.dron.news.domain.entity.Language
import com.dron.news.domain.entity.RefreshConfig
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getAllSubscriptions(): Flow<List<String>>
    fun startBackgroundRefresh(refreshConfig: RefreshConfig)
    suspend fun addSubscription(topic: String)
    suspend fun updateArticlesForTopic(topic: String, language: Language): Boolean
    suspend fun removeSubscription(topic: String)
    suspend fun updateArticlesForAllSubscription(language: Language): List<String>
    fun getArticlesByTopics(topics: List<String>): Flow<List<Article>>
    suspend fun clearAllArticles(topics: List<String>)
}