package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    suspend operator fun invoke(feedId: String, content: String) {
        val user = getUserUseCase().first() ?: throw NoUserFoundException()
        feedRepository.addComment(feedId, user, content)
    }
}