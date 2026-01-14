package com.dhkim.domain.feed.useCase

import androidx.paging.PagingData
import com.dhkim.domain.feed.model.LikeUser
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLikersUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(feedId: String): Flow<PagingData<LikeUser>> = feedRepository.getLikeUsers(feedId)
}