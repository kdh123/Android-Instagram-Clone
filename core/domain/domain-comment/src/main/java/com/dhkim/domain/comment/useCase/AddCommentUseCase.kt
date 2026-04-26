package com.dhkim.domain.comment.useCase

import com.dhkim.domain.comment.model.Comment
import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.repository.CommentRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val getUserUseCase: GetUserUseCase
) {

    suspend operator fun invoke(feedId: String, content: String, commentType: CommentType): Comment {
        val user = getUserUseCase().first() ?: throw NoUserFoundException()
        return commentRepository.addComment(feedId, user, content, commentType)
    }
}