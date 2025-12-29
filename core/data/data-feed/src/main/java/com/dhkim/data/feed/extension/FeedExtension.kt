package com.dhkim.data.feed.extension

import com.dhkim.database.entity.FeedUploadStatusEntity
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.UploadState

fun FeedUploadStatusEntity.toFeedUploadStatus(): FeedUploadStatus {
    return FeedUploadStatus(
        feedId = feedId,
        thumbnail = thumbnail,
        imageUrls = imageUrls,
        imageStatus = when (imageStatus) {
            0 -> UploadState.LOADING
            1 -> UploadState.SUCCESS
            2 -> UploadState.FAIL
            else -> UploadState.IDLE
        },
        contentStatus = when (contentStatus) {
            0 -> UploadState.LOADING
            1 -> UploadState.SUCCESS
            2 -> UploadState.FAIL
            else -> UploadState.IDLE
        },
    )
}

fun FeedUploadStatus.toEntity(): FeedUploadStatusEntity {
    return FeedUploadStatusEntity(
        feedId = feedId,
        thumbnail = thumbnail,
        imageUrls = imageUrls,
        imageStatus = imageStatus.code,
        contentStatus = contentStatus.code,
    )
}