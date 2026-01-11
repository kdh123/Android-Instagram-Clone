package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleEnableCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val getUserUseCase: GetUserUseCase
) {

    suspend operator fun invoke(feedId: String) {
        val userId = getUserUseCase().first()?.id ?: throw NoUserFoundException()
        feedRepository.toggleEnableComment(feedId, userId)
    }
}