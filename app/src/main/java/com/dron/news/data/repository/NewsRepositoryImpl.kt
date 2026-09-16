package com.dron.news.data.repository

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import com.dron.news.data.background.RefreshDataWorker
import com.dron.news.data.local.ArticleDbModel
import com.dron.news.data.local.NewDao
import com.dron.news.data.local.SubscriptionDbModel
import com.dron.news.data.mapper.toDbModels
import com.dron.news.data.mapper.toEntities
import com.dron.news.data.mapper.toQueryParam
import com.dron.news.data.mapper.toRefreshConfig
import com.dron.news.data.remote.NewsApiService
import com.dron.news.domain.entity.Article
import com.dron.news.domain.entity.Language
import com.dron.news.domain.entity.RefreshConfig
import com.dron.news.domain.repository.NewsRepository
import com.dron.news.domain.repository.SettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class NewsRepositoryImpl @Inject constructor(
    private val newsDao: NewDao,
    private val newsApiService: NewsApiService,
    private val workManager: WorkManager
) : NewsRepository {

    override fun getAllSubscriptions(): Flow<List<String>> {
        return newsDao.getAllSubscription().map { subscriptions ->
            subscriptions.map { it.topic }
        }
    }

    override suspend fun addSubscription(topic: String) {
        newsDao.addSubscription(SubscriptionDbModel(topic))

    }

    override suspend fun updateArticlesForTopic(topic: String, language: Language): Boolean {
        return try {
            val articles = loadArticles(topic, language)
            if (articles.isNotEmpty()) {
                newsDao.addArticles(articles)
                return true  // ← успех, если данные загружены
            }
            false
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error updating $topic", e)
            false
        }
    }

        private suspend fun loadArticles(topic: String, language: Language): List<ArticleDbModel> {
            return try {
                newsApiService.loadArticles(topic, language.toQueryParam()).toDbModels(topic)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                Log.e("NewsRepository", e.stackTraceToString())
                listOf()
            }
        }

    override suspend fun removeSubscription(topic: String) {
        newsDao.deleteSubscription(SubscriptionDbModel(topic))
    }

    override suspend fun updateArticlesForAllSubscription(language: Language): List<String> {
        val updatedTopics = ConcurrentLinkedQueue<String>()
        val subscriptions = newsDao.getAllSubscription().first()

        coroutineScope {
            subscriptions.forEach {
                launch {
                    val updated = updateArticlesForTopic(it.topic, language)
                    if (updated) {
                        updatedTopics.add(it.topic)
                    }
                }
            }
        }
        return updatedTopics.toList()
    }

    override fun getArticlesByTopics(topics: List<String>): Flow<List<Article>> {
        return newsDao.getAllArticlesByTopics(topics).map {
            it.toEntities()
        }
    }

    override suspend fun clearAllArticles(topics: List<String>) {
        newsDao.deleteArticlesByTopics(topics)
    }

    override fun startBackgroundRefresh(refreshConfig: RefreshConfig) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (refreshConfig.wifiOnly) {
                    NetworkType.UNMETERED
                } else {
                    NetworkType.CONNECTED
                }
            )
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<RefreshDataWorker>(
            refreshConfig.interval.minutes.toLong(), TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "Refresh Data",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request =request

        )
    }
}
