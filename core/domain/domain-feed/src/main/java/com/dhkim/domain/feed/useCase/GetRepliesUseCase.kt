package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Reply
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRepliesUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(commentId: String): Flow<List<Reply>> {
        return feedRepository.getReplies(commentId)
    }
}