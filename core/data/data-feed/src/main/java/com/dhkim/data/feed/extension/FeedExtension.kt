package com.dhkim.data.feed.extension

import com.dhkim.database.entity.FeedUploadStatusEntity
import com.dhkim.database.entity.HiddenFeedEntity
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.model.UploadState

fun FeedUploadStatusEntity.toFeedUploadStatus(): FeedUploadStatus {
    return FeedUploadStatus(
        feedId = feedId,
        thumbnail = thumbnail,
        imageUrls = imageUrls,
        uploadState = when (uploadStatus) {
            0 -> UploadState.LOADING
            1 -> UploadState.IMAGE_SUCCESS
            2 -> UploadState.FAIL
            3 -> UploadState.COMPLETE
            else -> UploadState.IDLE
        },
        shouldUpload = shouldUpload
    )
}

fun FeedUploadStatus.toEntity(): FeedUploadStatusEntity {
    return FeedUploadStatusEntity(
        feedId = feedId,
        thumbnail = thumbnail,
        imageUrls = imageUrls,
        uploadStatus = uploadState.code,
        shouldUpload = shouldUpload
    )
}

fun HiddenFeedEntity.toHiddenFeed(): HiddenFeed {
    return HiddenFeed(
        feedId = feedId,
        hiddenAt = hiddenAt
    )
}

fun HiddenFeed.toEntity(): HiddenFeedEntity {
    return HiddenFeedEntity(
        feedId = feedId,
        hiddenAt = hiddenAt
    )
}