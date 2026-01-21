package com.dhkim.data.feed.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ReplyDto(
    val replyId: String = "reply_id_${System.currentTimeMillis()}",
    val commentId: String = "",
    val user: UserDto = UserDto(),
    val content: String = "",
    val timeAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0
)