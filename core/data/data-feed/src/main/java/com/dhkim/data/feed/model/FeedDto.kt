package com.dhkim.data.feed.model

import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FeedDto(
    val type: String = "FOLLOWING",
    val feedId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val imageUrls: List<String> = listOf(),
    val caption: String = "",
    val timestamp: Long = 0L,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val adUrl: String = "",
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
            commentCount = commentCount,
            adUrl = adUrl
        )
    }
}

fun Feed.toDto(): FeedDto {
    return FeedDto(
        type = type,
        feedId = feedId,
        userId = userId,
        userName = userName,
        userProfileImage = userProfileImage,
        imageUrls = imageUrls,
        caption = caption,
        timestamp = timestamp,
        likeCount = likeCount,
        commentCount = commentCount,
        adUrl = adUrl
    )
}
