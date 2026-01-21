package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, commentId: String) {
        feedRepository.deleteComment(feedId, commentId)
    }
}