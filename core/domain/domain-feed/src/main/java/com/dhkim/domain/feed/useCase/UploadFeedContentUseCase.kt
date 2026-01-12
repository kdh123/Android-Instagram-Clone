package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UploadFeedContentUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    operator fun invoke(caption: String, imageUrls: List<String>): Flow<Unit> {
        return flow {
            val user = getUserUseCase().first() ?: throw NoUserFoundException()
            val feed = Feed(
                feedId = "feedId_${System.currentTimeMillis()}",
                userId = user.id,
                userName = user.name,
                userProfileImage = user.profileUrl,
                imageUrls = imageUrls,
                caption = caption,
            )
            feedRepository.uploadFeed(feed, userId = user.id).first()
            emit(Unit)
        }
    }
}