package com.dhkim.data.feed.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CommentDto(
    val commentId: String = "comment_id_${System.currentTimeMillis()}",
    val feedId: String = "",
    val userDto: UserDto = UserDto(),
    val content: String = "",
    val timeAt: Long = System.currentTimeMillis(),
    val replyCount: Int = 0,
    val likeCount: Int = 0
)