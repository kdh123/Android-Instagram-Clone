package com.dhkim.feed.common

import com.dhkim.domain.feed.model.Feed
import kotlinx.collections.immutable.toImmutableList

fun Feed.toFeedItem(myUserId: String): FeedItem {
    val feedType = if (type.isEmpty()) {
        when {
            myUserId == userId -> FeedItemType.Mine(imageUrls = imageUrls.toImmutableList(), timestamp = timestamp.toRelativeTime())
            else -> FeedItemType.Suggested(imageUrls = imageUrls.toImmutableList(), timestamp = timestamp.toRelativeTime())
        }
    } else {
        when (type.uppercase()) {
            FeedType.MINE.name -> FeedItemType.Mine(imageUrls = imageUrls.toImmutableList(), timestamp = timestamp.toRelativeTime())
            FeedType.FOLLOWING.name -> FeedItemType.Following(imageUrls = imageUrls.toImmutableList(), timestamp = timestamp.toRelativeTime())
            FeedType.SUGGESTED.name -> FeedItemType.Suggested(imageUrls = imageUrls.toImmutableList(), timestamp = timestamp.toRelativeTime())
            FeedType.SPONSORED.name -> FeedItemType.Sponsored(imageUrl = imageUrls.firstOrNull() ?: "", adUrl = adUrl)
            else -> FeedItemType.Suggested(imageUrls = imageUrls.toImmutableList(), timestamp = timestamp.toRelativeTime())
        }
    }

    return FeedItem(
        feedId = feedId,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        caption = caption,
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = isLiked,
        type = feedType,
        isLikeCountVisible = isLikeCountVisible,
        isCommentEnabled = isCommentEnabled
    )
}