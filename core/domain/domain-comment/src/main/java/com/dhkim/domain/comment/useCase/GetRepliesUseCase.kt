package com.dhkim.domain.comment.useCase

import com.dhkim.domain.comment.model.Reply
import com.dhkim.domain.comment.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRepliesUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {

    operator fun invoke(commentId: String): Flow<List<Reply>> {
        return commentRepository.getReplies(commentId)
    }
}