package com.dhkim.domain.feed.model

data class LikeFeed(
    val feedId: String,
    val userId: String,
    val likedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)