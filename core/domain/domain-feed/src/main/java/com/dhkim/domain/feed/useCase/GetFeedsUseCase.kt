package com.dhkim.domain.feed.useCase

import androidx.paging.PagingData
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(): Flow<PagingData<Feed>> {
        return feedRepository.getFeeds()
    }
}