package com.dhkim.domain.feed.model

import com.dhkim.domain.user.model.User

data class Reply(
    val replyId:String,
    val commentId: String,
    val user: User,
    val content: String,
    val timeAt: Long,
    val likeCount: Int
)
