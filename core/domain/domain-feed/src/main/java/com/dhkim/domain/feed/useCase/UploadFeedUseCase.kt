package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NouUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UploadFeedUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    operator fun invoke(feed: Feed): Flow<Unit> {
        return flow {
            val user = getUserUseCase().first() ?: throw NouUserFoundException()
            val updateFeed = feed.copy(userId = user.id)
            feedRepository.uploadFeed(updateFeed).first()
            feedRepository.uploadImages(updateFeed.userId, updateFeed.imageUrls).first()
            emit(Unit)
        }
    }
}