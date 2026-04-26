package com.dhkim.data.comment.model

import com.dhkim.data.user.model.UserDto
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CommentDto(
    val commentId: String = "comment_id_${System.currentTimeMillis()}",
    val targetId: String = "",
    val user: UserDto = UserDto(),
    val content: String = "",
    val timeAt: Long = System.currentTimeMillis(),
    val replyCount: Int = 0,
    val likeCount: Int = 0
)