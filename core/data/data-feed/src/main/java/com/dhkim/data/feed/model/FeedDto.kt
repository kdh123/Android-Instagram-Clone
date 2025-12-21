package com.dhkim.data.feed.model

import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FeedDto(
    val feedId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val imageUrls: List<String> = listOf(),
    val caption: String = "",
    val timestamp: Long = 0L,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
) {
    fun toFeed(): Feed {
        return Feed(
            feedId = feedId,
            userId = userId,
            userName = userName,
            userProfileImage = userProfileImage,
            imageUrls = imageUrls,
            caption = caption,
            timestamp = timestamp,
            likeCount = likeCount,
            commentCount = commentCount
        )
    }
}

fun Feed.toDto(): FeedDto {
    return FeedDto(
        feedId = feedId,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        imageUrls = imageUrls,
        caption = caption,
        timestamp = timestamp,
        likeCount = likeCount,
        commentCount = commentCount
    )
}
