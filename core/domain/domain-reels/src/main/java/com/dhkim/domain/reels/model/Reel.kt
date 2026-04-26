package com.dhkim.domain.reels.model

data class Reel(
    val id: String,
    val url: String,
    val caption: String,
    val likeCount: Int,
    val commentCount: Int,
    val userId: String,
    val userName: String,
    val userProfileImage: String,
    val playbackPosition: Long = 0,
    val isLiked: Boolean = false,
)