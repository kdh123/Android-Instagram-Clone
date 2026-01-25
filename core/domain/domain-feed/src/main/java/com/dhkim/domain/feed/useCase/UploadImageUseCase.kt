package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.UUID
import javax.inject.Inject

typealias ImageDownloadUrl = String

class UploadImageUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    operator fun invoke(file: File): Flow<ImageDownloadUrl> {
        return flow {
            val user = getUserUseCase().first() ?: throw NoUserFoundException()
            val storagePath = "feeds/${user.id}/photo_${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg"
            val imageDownloadUrl = feedRepository.uploadImage(storagePath, file).first()
            emit(imageDownloadUrl)
        }
    }
}