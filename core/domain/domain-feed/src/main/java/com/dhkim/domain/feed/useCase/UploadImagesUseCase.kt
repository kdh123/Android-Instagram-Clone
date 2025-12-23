package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NouUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UploadImagesUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    context(CoroutineScope)
    operator fun invoke(feed: Feed, imageUrls: List<String>): Flow<Unit> {
        return flow {
            val user = getUserUseCase().first() ?: throw NouUserFoundException()
            val uploadImageJobs = mutableListOf<Deferred<Unit>>()
            val feedImageUrls = mutableListOf<String>()
            for (imageUrl in imageUrls) {
                val filePath = "feeds/${user.id}/photo_${System.currentTimeMillis()}.jpg"
                val job = async { feedRepository.uploadImages(filePath, imageUrl).first() }
                uploadImageJobs.add(job)
                feedImageUrls.add(filePath)
            }
            uploadImageJobs.awaitAll()
            emit(Unit)
        }
    }
}