package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Reply
import com.dhkim.domain.feed.repository.FeedRepository
import javax.inject.Inject

class DeleteReplyUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    suspend operator fun invoke(feedId: String, commentId: String, replyId: String): Reply? {
        return feedRepository.deleteReply(feedId, commentId, replyId)
    }
}