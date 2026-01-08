package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import javax.inject.Inject

class UpdateEnableFeedCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, isEnabled: Boolean) {
        feedRepository.updateCommentVisibility(feedId, isEnabled)
    }
}