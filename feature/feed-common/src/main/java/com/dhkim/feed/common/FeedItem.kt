package com.dhkim.feed.common

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed class FeedItem(
    open val feedId: String,
    open val userId: String,
    open val userName: String,
    open val userProfileImage: String,
    open val caption: String,
    open val likeCount: Int,
    open val commentCount: Int,
    open val isLiked: Boolean
) {

    data class Mine(
        override val feedId: String,
        override val userId: String,
        override val userName: String,
        override val userProfileImage: String,
        override val caption: String,
        override val likeCount: Int,
        override val commentCount: Int,
        override val isLiked: Boolean,
        val imageUrls: ImmutableList<String>,
        val timestamp: Timestamp
    ) : FeedItem(
        feedId = feedId,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        caption = caption,
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = isLiked
    )

    data class Following(
        override val feedId: String,
        override val userId: String,
        override val userName: String,
        override val userProfileImage: String,
        override val caption: String,
        override val likeCount: Int,
        override val commentCount: Int,
        override val isLiked: Boolean,
        val imageUrls: ImmutableList<String>,
        val timestamp: Timestamp
    ) : FeedItem(
        feedId = feedId,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        caption = caption,
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = isLiked
    )

    data class Suggested(
        override val feedId: String,
        override val userId: String,
        override val userName: String,
        override val userProfileImage: String,
        override val caption: String,
        override val likeCount: Int,
        override val commentCount: Int,
        override val isLiked: Boolean,
        val imageUrls: ImmutableList<String>,
        val timestamp: Timestamp,
    ) : FeedItem(
        feedId = feedId,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        caption = caption,
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = isLiked
    )

    data class Sponsored(
        override val feedId: String,
        override val userId: String,
        override val userName: String,
        override val userProfileImage: String,
        override val caption: String,
        override val likeCount: Int,
        override val commentCount: Int,
        override val isLiked: Boolean,
        val imageUrl: String,
        val adUrl: String,
    ) : FeedItem(
        feedId = feedId,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        caption = caption,
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = isLiked
    )
}