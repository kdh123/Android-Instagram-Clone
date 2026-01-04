package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHiddenFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(): Flow<Set<HiddenFeed>> {
        return feedRepository.getHiddenFeeds()
    }
}