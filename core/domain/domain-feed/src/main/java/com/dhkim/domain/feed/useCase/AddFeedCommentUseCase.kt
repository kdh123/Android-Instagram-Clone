package com.dhkim.domain.feed.useCase

import com.dhkim.domain.comment.model.Comment
import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.useCase.AddCommentUseCase
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddFeedCommentUseCase @Inject constructor(
    private val addCommentUseCase: AddCommentUseCase,
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, content: String): Comment {
        val comment = addCommentUseCase(feedId, content, CommentType.FEED)

        val currentHomeFeed = feedRepository.getHomeFeed(feedId).first()
        if (currentHomeFeed != null) {
            val commentCount = currentHomeFeed.commentCount
            feedRepository.updateHomeFeed(currentHomeFeed.copy(commentCount = commentCount + 1))
        }
        return comment
    }
}