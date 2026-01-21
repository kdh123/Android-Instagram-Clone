package com.dhkim.feed.common

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ReplyGroup(
    val commentId: String = "",
    val replies: ImmutableList<ReplyItem> = persistentListOf(),
    val recentAddedReplies: ImmutableList<ReplyItem> = persistentListOf()
)

@Immutable
data class ReplyItem(
    val replyId:String,
    val user: UserItem,
    val content: String,
    val timeAt: Timestamp,
    val likeCount: Int
)


