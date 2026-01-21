package com.dhkim.feed.common

import androidx.compose.runtime.Immutable

@Immutable
data class CommentItem(
    val commentId: String,
    val userId: String,
    val userName: String,
    val userProfileImageUrl: String,
    val content: String,
    val timeAt: Timestamp,
    val replyCount: Int,
    val likeCount: Int,
)