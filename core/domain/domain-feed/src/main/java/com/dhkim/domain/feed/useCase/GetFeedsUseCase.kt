package com.dhkim.domain.feed.useCase

import androidx.paging.PagingData
import androidx.paging.filter
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(): Flow<PagingData<Feed>> {
        return feedRepository.getHiddenFeeds()
            .flatMapLatest { hiddenFeed ->
                val hiddenIds = hiddenFeed.map { it.feedId }.toSet()
                feedRepository.getHomeFeeds().map { pagingData ->
                    pagingData
                        .filter { it.feedId !in hiddenIds }
                }
            }
    }
}