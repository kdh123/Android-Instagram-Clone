package com.dhkim.data.feed.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhkim.domain.feed.repository.FeedRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class FeedLikeSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val feedRepository: FeedRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val feedId = inputData.getString("KEY_FEED_ID") ?: return Result.failure()
        val userId = inputData.getString("KEY_USER_ID") ?: return Result.failure()

        feedRepository.remoteToggleLike(feedId, userId)
        val likeFeed = feedRepository.getLikeFeed(feedId, userId).first()
        if (likeFeed != null) {
            feedRepository.updateLikeFeed(likeFeed.copy(isSynced = true))
        }
        return Result.success()
    }
}