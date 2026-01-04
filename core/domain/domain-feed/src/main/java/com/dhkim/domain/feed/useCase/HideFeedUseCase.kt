package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HideFeedUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    operator fun invoke(feedId: String): Flow<Unit> {
        return flow {
            val userId = getUserUseCase().first()?.id ?: throw NoUserFoundException()
            val hiddenFeed = HiddenFeed(feedId = feedId, hiddenAt = System.currentTimeMillis())
            feedRepository.hideFeed(userId, hiddenFeed)
            emit(Unit)
        }
    }
}