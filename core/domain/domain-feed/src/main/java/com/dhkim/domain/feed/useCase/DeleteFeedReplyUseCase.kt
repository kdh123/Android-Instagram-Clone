package com.dhkim.domain.feed.useCase

import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.model.Reply
import com.dhkim.domain.comment.useCase.DeleteReplyUseCase
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteFeedReplyUseCase @Inject constructor(
    private val deleteReplyUseCase: DeleteReplyUseCase,
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, commentId: String, replyId: String): Reply? {
        val reply = deleteReplyUseCase(feedId, commentId, replyId, CommentType.FEED)
        val currentHomeFeed = feedRepository.getHomeFeed(feedId).first()
        if (currentHomeFeed != null) {
            feedRepository.updateHomeFeed(currentHomeFeed.copy(commentCount = currentHomeFeed.commentCount - 1))
        }
        return reply
    }
}