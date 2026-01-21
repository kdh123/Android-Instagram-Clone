package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Reply
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReplyCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    suspend operator fun invoke(feedId: String, commentId: String, comment: String): Reply {
        val user = getUserUseCase().first() ?: throw NoUserFoundException()
        return feedRepository.replyComment(feedId, commentId, user, comment)
    }
}