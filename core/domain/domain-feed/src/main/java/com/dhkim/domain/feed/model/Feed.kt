package com.dhkim.domain.feed.model

data class Feed(
    val type: String = "",
    val feedId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val imageUrls: List<String> = listOf(),
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val adUrl: String = "",
    val isLikeCountVisible: Boolean = true,
    val isCommentEnabled: Boolean = true,
)