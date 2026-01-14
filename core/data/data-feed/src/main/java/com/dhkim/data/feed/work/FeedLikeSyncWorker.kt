package com.dhkim.data.feed.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.useCase.GetUserUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class FeedLikeSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val feedId = inputData.getString("KEY_FEED_ID") ?: return Result.failure()
        val user = getUserUseCase().first() ?: return Result.failure()
        val userId = user.id

        feedRepository.remoteToggleLike(feedId, user)
        val likeFeed = feedRepository.getLikeFeed(feedId, userId).first()
        if (likeFeed != null) {
            feedRepository.updateLikeFeed(likeFeed.copy(isSynced = true))
        }
        return Result.success()
    }
}