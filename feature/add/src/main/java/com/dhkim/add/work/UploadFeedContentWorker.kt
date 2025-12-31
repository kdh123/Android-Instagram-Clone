package com.dhkim.add.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhkim.domain.feed.model.UploadState
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.UploadFeedContentUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformWhile

@HiltWorker
class UploadFeedContentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val uploadFeedContentUseCase: UploadFeedContentUseCase,
    private val feedRepository: FeedRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val feedId = inputData.getString("KEY_FEED_ID") ?: return Result.failure()
        val feedCaption = inputData.getString("KEY_FEED_CAPTION") ?: ""

        feedRepository.getFeedUploadStatus(feedId)
            .transformWhile { status ->
                emit(status)
                status == null || status.uploadState == UploadState.LOADING
            }.collect { feedUploadStatus ->
                val imageUploadState = feedUploadStatus?.uploadState ?: return@collect
                when (imageUploadState) {
                    UploadState.LOADING,
                    UploadState.COMPLETE -> Unit

                    UploadState.IDLE,
                    UploadState.FAIL -> {
                        updateFeedUploadFailStatus(feedId)
                    }

                    UploadState.IMAGE_SUCCESS -> {
                        try {
                            uploadFeedContentUseCase(feedCaption, feedUploadStatus.imageUrls).first()
                            updateFeedUploadSuccessStatus(feedId)
                        } catch (_: Exception) {
                            updateFeedUploadFailStatus(feedId)
                        }
                    }
                }
            }

        return Result.Success()
    }

    private suspend fun updateFeedUploadSuccessStatus(feedId: String) {
        val feedUploadLoadingStatus = feedRepository.getFeedUploadStatus(feedId).first()?.copy(
            uploadState = UploadState.COMPLETE
        ) ?: return
        feedRepository.insertFeedUploadStatus(feedUploadLoadingStatus)
    }

    private suspend fun updateFeedUploadFailStatus(feedId: String) {
        val feedUploadLoadingStatus = feedRepository.getFeedUploadStatus(feedId).first()?.copy(
            uploadState = UploadState.FAIL
        ) ?: return
        feedRepository.insertFeedUploadStatus(feedUploadLoadingStatus)
    }
}