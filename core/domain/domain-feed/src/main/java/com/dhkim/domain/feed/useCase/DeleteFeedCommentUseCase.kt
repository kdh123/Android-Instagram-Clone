package com.dhkim.domain.feed.useCase

import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.useCase.DeleteCommentUseCase
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteFeedCommentUseCase @Inject constructor(
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, commentId: String) {
        val remainingReplyCount = deleteCommentUseCase(feedId, commentId, CommentType.FEED)
        val currentHomeFeed = feedRepository.getHomeFeed(feedId).first()
        if (currentHomeFeed != null) {
            feedRepository.updateHomeFeed(currentHomeFeed.copy(commentCount = remainingReplyCount))
        }
    }
}