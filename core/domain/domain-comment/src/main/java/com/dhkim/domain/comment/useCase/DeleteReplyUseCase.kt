package com.dhkim.domain.comment.useCase

import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.model.Reply
import com.dhkim.domain.comment.repository.CommentRepository
import javax.inject.Inject

class DeleteReplyUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
) {

    suspend operator fun invoke(feedId: String, commentId: String, replyId: String, commentType: CommentType): Reply? {
        return commentRepository.deleteReply(feedId, commentId, replyId, commentType)
    }
}