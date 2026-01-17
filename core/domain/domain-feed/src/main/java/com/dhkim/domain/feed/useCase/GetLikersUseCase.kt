package com.dhkim.domain.feed.useCase

import androidx.paging.PagingData
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLikersUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(feedId: String): Flow<PagingData<User>> = feedRepository.getLikeUsers(feedId)
}