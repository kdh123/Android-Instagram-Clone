package com.dhkim.domain.comment.useCase

import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.repository.CommentRepository
import javax.inject.Inject

typealias RemainingReplyCount = Int

class DeleteCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {

    suspend operator fun invoke(feedId: String, commentId: String, commentType: CommentType): RemainingReplyCount {
        return commentRepository.deleteComment(feedId, commentId, commentType)
    }
}