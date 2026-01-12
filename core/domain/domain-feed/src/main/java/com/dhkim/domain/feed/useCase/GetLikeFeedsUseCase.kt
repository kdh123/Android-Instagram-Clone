package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.LikeFeed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetLikeFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    operator fun invoke(): Flow<Set<LikeFeed>> {
        return getUserUseCase().flatMapLatest { user ->
            val userId = user?.id ?: return@flatMapLatest flowOf(emptySet())
            feedRepository.getAllLikedFeeds(userId)
        }
    }
}