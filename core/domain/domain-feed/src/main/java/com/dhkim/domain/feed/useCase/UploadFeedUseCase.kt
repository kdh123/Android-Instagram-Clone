package com.dhkim.domain.feed.useCase

import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import javax.inject.Inject

class UploadFeedUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {

    operator fun invoke(feed: Feed) {
        feedRepository.uploadFeed(feed)
    }
}