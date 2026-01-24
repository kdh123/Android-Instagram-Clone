package com.dhkim.add.work

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.UploadState
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.UploadImageUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.toList
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.core.graphics.scale
import kotlinx.coroutines.flow.retry
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@HiltWorker
class UploadFeedImagesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val uploadImageUseCase: UploadImageUseCase,
    private val feedRepository: FeedRepository
) : CoroutineWorker(context, workerParams) {

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        val feedId = inputData.getString("KEY_FEED_ID") ?: return Result.failure()
        val filePathArray = inputData.getStringArray("KEY_IMAGE_URIS")
        return try {
            val filePaths = filePathArray?.toList() ?: listOf()
            if (filePaths.isEmpty()) Result.failure()
            updateFeedUploadLoadingStatus(feedId = feedId, firstImagePath = filePaths.first())
            uploadImages(feedId, filePaths)
            Result.Success()
        } catch (_: Exception) {
            updateFeedUploadFailStatus(feedId)
            Result.failure()
        }
    }

    private suspend fun uploadImages(feedId: String, filePaths: List<String>) = coroutineScope {
        val downloadImageUrls = filePaths.asFlow()
            .flatMapMerge(concurrency = 10) { filePath ->
                val file = File(filePath)
                uploadImageUseCase(file)
                    .retry(2) { e -> e is IOException }
                    .catch { emit("") }
            }.toList()
            .filter { it.isNotEmpty() }
        val feedUploadStatus = feedRepository.getFeedUploadStatus(feedId).first()?.copy(
            imageUrls = downloadImageUrls,
            uploadState = UploadState.IMAGE_SUCCESS
        ) ?: return@coroutineScope
        feedRepository.insertFeedUploadStatus(feedUploadStatus)
    }

    private suspend fun updateFeedUploadLoadingStatus(feedId: String, firstImagePath: String) {
        val feedUploadLoadingStatus = FeedUploadStatus(
            feedId = feedId,
            thumbnail = getThumbnailByteArray(firstImagePath),
            imageUrls = listOf(),
            uploadState = UploadState.LOADING,
            shouldUpload = false
        )
        feedRepository.insertFeedUploadStatus(feedUploadLoadingStatus)
    }

    private suspend fun updateFeedUploadFailStatus(feedId: String) {
        val feedUploadLoadingStatus = feedRepository.getFeedUploadStatus(feedId).first()?.copy(
            uploadState = UploadState.FAIL,
        ) ?: return
        feedRepository.insertFeedUploadStatus(feedUploadLoadingStatus)
    }

    private fun getThumbnailByteArray(filePath: String): ByteArray {
        val file = File(filePath)
        return if (file.exists()) {
            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
            val thumbnail = originalBitmap.scale(100, 100)
            val outputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.toByteArray()
        } else {
            ByteArray(0)
        }
    }
}