package com.dhkim.domain.feed.useCase

import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.model.Reply
import com.dhkim.domain.comment.useCase.ReplyCommentUseCase
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReplyFeedCommentUseCase @Inject constructor(
    private val replyCommentUseCase: ReplyCommentUseCase,
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, commentId: String, comment: String): Reply {
        val reply = replyCommentUseCase(feedId, commentId, comment, CommentType.FEED)
        val currentHomeFeed = feedRepository.getHomeFeed(feedId).first()
        if (currentHomeFeed != null) {
            feedRepository.updateHomeFeed(currentHomeFeed.copy(commentCount = currentHomeFeed.commentCount + 1))
        }
        return reply
    }
}