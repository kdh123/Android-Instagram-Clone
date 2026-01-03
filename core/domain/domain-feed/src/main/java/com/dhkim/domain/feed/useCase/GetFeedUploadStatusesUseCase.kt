package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFeedUploadStatusesUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(): Flow<List<FeedUploadStatus>> {
        return feedRepository.getFeedUploadStatuses()
            .map { statuses ->
                statuses.filter { it.shouldUpload }
            }
    }
}