package com.dhkim.feed.common

import com.dhkim.domain.feed.model.Feed
import kotlinx.collections.immutable.toImmutableList

fun Feed.toFeedItem(myUserId: String): FeedItem {
    val feedType = try {
        if (type.isEmpty()) {
            when {
                myUserId == userId -> FeedType.MINE
                else -> FeedType.SUGGESTED
            }
        } else {
            FeedType.valueOf(type.uppercase())
        }
    } catch (_: IllegalArgumentException) {
        FeedType.SUGGESTED
    }

    when (feedType) {
        FeedType.MINE -> {
            return FeedItem.Mine(
                feedId = feedId,
                userId = userId,
                userName = userName,
                userProfileImage = userProfileImage,
                caption = caption,
                likeCount = likeCount,
                commentCount = commentCount,
                isLiked = isLiked,
                imageUrls = imageUrls.toImmutableList(),
                timestamp = timestamp.toRelativeTime()
            )
        }

        FeedType.FOLLOWING -> {
            return FeedItem.Following(
                feedId = feedId,
                userId = userId,
                userName = userName,
                userProfileImage = userProfileImage,
                caption = caption,
                likeCount = likeCount,
                commentCount = commentCount,
                isLiked = isLiked,
                imageUrls = imageUrls.toImmutableList(),
                timestamp = timestamp.toRelativeTime()
            )
        }

        FeedType.SUGGESTED -> {
            return FeedItem.Suggested(
                feedId = feedId,
                userId = userId,
                userName = userName,
                userProfileImage = userProfileImage,
                caption = caption,
                likeCount = likeCount,
                commentCount = commentCount,
                isLiked = isLiked,
                imageUrls = imageUrls.toImmutableList(),
                timestamp = timestamp.toRelativeTime()
            )
        }

        FeedType.SPONSORED -> {
            return FeedItem.Sponsored(
                feedId = feedId,
                userId = userId,
                userName = userName,
                userProfileImage = userProfileImage,
                caption = caption,
                likeCount = likeCount,
                commentCount = commentCount,
                isLiked = isLiked,
                imageUrl = imageUrls.firstOrNull() ?: "",
                adUrl = adUrl
            )
        }
    }
}