package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NouUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

typealias ImageDownloadUrl = String

class UploadImageUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    operator fun invoke(byteArray: ByteArray): Flow<ImageDownloadUrl> {
        return flow {
            val user = getUserUseCase().first() ?: throw NouUserFoundException()
            val filePath = "feeds/${user.id}/photo_${System.currentTimeMillis()}.jpg"
            val imageDownloadUrl = feedRepository.uploadImage(filePath, byteArray).first()
            emit(imageDownloadUrl)
        }
    }
}