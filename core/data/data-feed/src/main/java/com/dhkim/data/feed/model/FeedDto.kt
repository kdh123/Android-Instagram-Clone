package com.dhkim.data.feed.model

import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FeedDto(
    val type: String = "",
    val feedId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val imageUrls: List<String> = listOf(),
    val caption: String = "",
    val timestamp: Long = 0L,
    val likeCount: Int = 0,
    val representativeLikerName: String = "",
    val representativeLikerId: String = "",
    val commentCount: Int = 0,
    val adUrl: String = "",
    val showLikeCount: Boolean = true,
    val commentEnabled: Boolean = true,
) {
    fun toFeed(): Feed {
        return Feed(
            type = type,
            feedId = feedId,
            userId = userId,
            userName = userName,
            userProfileImage = userProfileImage,
            imageUrls = imageUrls,
            caption = caption,
            timestamp = timestamp,
            likeCount = likeCount,
            representativeLikerName = representativeLikerName,
            representativeLikerId = representativeLikerId,
            commentCount = commentCount,
            adUrl = adUrl,
            isLikeCountVisible = showLikeCount,
            isCommentEnabled = commentEnabled
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
        representativeLikerName = representativeLikerName,
        representativeLikerId = representativeLikerId,
        commentCount = commentCount,
        adUrl = adUrl,
        showLikeCount = isLikeCountVisible,
        commentEnabled = isCommentEnabled
    )
}
