package com.dhkim.feed.common

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class FeedItem(
    val feedId: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String,
    val caption: String,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val type: FeedItemType
)

sealed interface FeedItemType {
    data class Mine(
        val imageUrls: ImmutableList<String>,
        val timestamp: Timestamp
    ) : FeedItemType

    data class Following(
        val imageUrls: ImmutableList<String>,
        val timestamp: Timestamp
    ) : FeedItemType

    data class Suggested(
        val imageUrls: ImmutableList<String>,
        val timestamp: Timestamp
    ) : FeedItemType

    data class Sponsored(
        val imageUrl: String,
        val adUrl: String,
    ) : FeedItemType
}