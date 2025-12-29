package com.dhkim.add.work

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.UploadState
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.ImageDownloadUrl
import com.dhkim.domain.feed.useCase.UploadImageUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import java.io.File

@HiltWorker
class UploadFeedImagesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val uploadImageUseCase: UploadImageUseCase,
    private val feedRepository: FeedRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result  {
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
        val uploadImageJobs = mutableListOf<Deferred<ImageDownloadUrl>>()

        filePaths.forEachIndexed { index, filePath ->
            val file = File(filePath)
            val job = async {
                uploadImageUseCase(file).first()
            }
            uploadImageJobs.add(job)
        }

        val downloadImageUrls = uploadImageJobs.awaitAll()
        val feedUploadStatus = feedRepository.getFeedUploadStatus(feedId).first()?.copy(
            imageUrls = downloadImageUrls,
            imageStatus = UploadState.SUCCESS
        ) ?: return@coroutineScope
        feedRepository.insertFeedUploadStatus(feedUploadStatus)
    }

    private suspend fun updateFeedUploadLoadingStatus(feedId: String, firstImagePath: String) {
        val feedUploadLoadingStatus = FeedUploadStatus(
            feedId = feedId,
            thumbnail = getThumbnailByteArray(firstImagePath),
            imageUrls = listOf(),
            imageStatus = UploadState.LOADING,
            contentStatus = UploadState.LOADING,
        )
        feedRepository.insertFeedUploadStatus(feedUploadLoadingStatus)
    }

    private suspend fun updateFeedUploadFailStatus(feedId: String) {
        val feedUploadLoadingStatus = feedRepository.getFeedUploadStatus(feedId).first()?.copy(
            imageStatus = UploadState.FAIL,
            contentStatus = UploadState.FAIL
        ) ?: return
        feedRepository.insertFeedUploadStatus(feedUploadLoadingStatus)
    }

    private fun getThumbnailByteArray(filePath: String): ByteArray {
        val file = File(filePath)
        return if (file.exists()) {
            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
            val thumbnail = Bitmap.createScaledBitmap(originalBitmap, 100, 100, true)
            val outputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.toByteArray()
        } else {
            ByteArray(0)
        }
    }
}