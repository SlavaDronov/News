package com.dron.news.domain.usecase

import com.dron.news.domain.repository.NewsRepository
import javax.inject.Inject

class GetAllSubscriptionUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke() = newsRepository.getAllSubscriptions()
}