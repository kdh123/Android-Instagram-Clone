package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import javax.inject.Inject

class GetLikeUsersUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(feedId: String) = feedRepository.getLikeUsers(feedId)
}