package com.dhkim.data.feed.extension

import com.dhkim.data.feed.model.FeedDto
import com.dhkim.data.feed.model.LikeFeedDto
import com.dhkim.database.entity.FeedUploadStatusEntity
import com.dhkim.database.entity.HiddenFeedEntity
import com.dhkim.database.entity.HomeFeedEntity
import com.dhkim.database.entity.LikeEntity
import com.dhkim.database.entity.SearchFeedEntity
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.model.LikeFeed
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

fun FeedDto.toHomeEntity(): HomeFeedEntity {
    return HomeFeedEntity(
        feedId = feedId,
        type = type,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        imageUrls = imageUrls,
        caption = caption,
        timestamp = timestamp,
        likeCount = likeCount,
        commentCount = commentCount,
        adUrl = adUrl,
        isLikeCountVisible = isLikeCountVisible,
        isCommentEnabled = isCommentEnabled
    )
}

fun HomeFeedEntity.toFeed(): Feed {
    return Feed(
        feedId = feedId,
        type = type,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        imageUrls = imageUrls,
        caption = caption,
        timestamp = timestamp,
        likeCount = likeCount,
        commentCount = commentCount,
        adUrl = adUrl,
        isLikeCountVisible = isLikeCountVisible,
        isCommentEnabled = isCommentEnabled
    )
}

fun FeedDto.toSearchEntity(): SearchFeedEntity {
    return SearchFeedEntity(
        feedId = feedId,
        type = type,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        imageUrls = imageUrls,
        caption = caption,
        timestamp = timestamp,
        likeCount = likeCount,
        commentCount = commentCount,
        adUrl = adUrl,
        isLikeCountVisible = isLikeCountVisible,
        isCommentEnabled = isCommentEnabled
    )
}

fun SearchFeedEntity.toFeed(): Feed {
    return Feed(
        feedId = feedId,
        type = type,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        imageUrls = imageUrls,
        caption = caption,
        timestamp = timestamp,
        likeCount = likeCount,
        commentCount = commentCount,
        adUrl = adUrl,
        isLikeCountVisible = isLikeCountVisible,
        isCommentEnabled = isCommentEnabled
    )
}

fun LikeEntity.toLikeFeed(): LikeFeed {
    return LikeFeed(
        feedId = feedId,
        userId = userId,
        likedAt = likedAt,
        isSynced = isSynced
    )
}

fun LikeFeed.toEntity(): LikeEntity {
    return LikeEntity(
        feedId = feedId,
        userId = userId,
        likedAt = likedAt,
        isSynced = isSynced
    )
}

fun LikeFeedDto.toLikeFeed(): LikeFeed {
    return LikeFeed(
        feedId = feedId,
        userId = userId,
        likedAt = isLikeAt,
        isSynced = true
    )
}