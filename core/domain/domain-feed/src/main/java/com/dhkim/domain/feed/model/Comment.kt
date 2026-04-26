package com.dhkim.domain.feed.model

import com.dhkim.domain.user.model.User

data class Comment(
    val commentId:String,
    val targetId: String,
    val user: User,
    val content: String,
    val timeAt: Long,
    val replyCount: Int,
    val likeCount: Int
)
