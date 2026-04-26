package com.dhkim.domain.comment.useCase

import androidx.paging.PagingData
import com.dhkim.domain.comment.model.Comment
import com.dhkim.domain.comment.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {

    operator fun invoke(feedId: String): Flow<PagingData<Comment>> {
        return commentRepository.getComments(feedId)
    }
}