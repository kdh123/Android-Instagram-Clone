package com.dhkim.main.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhkim.domain.feed.repository.FeedRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class RemoveUploadCompletedFeedUploadStateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val feedRepository: FeedRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        delay(10_000)

        val feedId = inputData.getString("KEY_FEED_ID") ?: return Result.failure()
        feedRepository.deleteFeedUploadStatus(feedId)

        return Result.Success()
    }
}