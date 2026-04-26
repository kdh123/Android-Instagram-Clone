package com.dhkim.domain.comment.useCase

import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.model.Reply
import com.dhkim.domain.comment.repository.CommentRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReplyCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val getUserUseCase: GetUserUseCase
) {

    suspend operator fun invoke(feedId: String, commentId: String, comment: String, commentType: CommentType): Reply {
        val user = getUserUseCase().first() ?: throw NoUserFoundException()
        return commentRepository.replyComment(feedId, commentId, user, comment, commentType)
    }
}