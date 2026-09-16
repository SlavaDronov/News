package com.dron.news.data.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dron.news.domain.usecase.GetSettingsUseCase
import com.dron.news.domain.usecase.UpdateSubscribedArticlesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first


@HiltWorker
class RefreshDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val updateSubscribedArticlesUseCase: UpdateSubscribedArticlesUseCase,
    private val notificationsHelper: NotificationsHelper,
    private val getSettingsUseCase: GetSettingsUseCase
): CoroutineWorker(
    context,
    workerParameters
) {
    override suspend fun doWork(): Result {
        Log.d("RefreshDataWorker", "Start")
        val settings = getSettingsUseCase().first()
        val updateTopics = updateSubscribedArticlesUseCase()
        if (updateTopics.isNotEmpty() && settings.notificationEnabled) {
            notificationsHelper.showNewArticlesNotification(updateTopics)
        }
        Log.d("RefreshDataWorker", "Finish")

        return Result.success()
    }
}