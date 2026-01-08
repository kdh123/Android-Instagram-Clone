package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import javax.inject.Inject

class UpdateFeedLikeCountVisibilityUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, isVisible: Boolean) {
        feedRepository.updateLikeCountVisibility(feedId, isVisible)
    }
}