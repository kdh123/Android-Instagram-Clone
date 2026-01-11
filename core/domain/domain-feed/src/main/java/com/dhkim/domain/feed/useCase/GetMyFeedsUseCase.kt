package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(): Flow<Set<Feed>> {
        return feedRepository.getMyFeeds()
    }
}