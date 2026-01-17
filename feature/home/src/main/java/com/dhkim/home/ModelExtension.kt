package com.dhkim.home

import com.dhkim.domain.feed.model.Comment
import com.dhkim.domain.user.model.User
import com.dhkim.feed.common.CommentItem
import com.dhkim.feed.common.UserItem
import com.dhkim.feed.common.toRelativeTime

fun User.toUserItem(): UserItem {
    return UserItem(
        id = id,
        name = name,
        profileImageUrl = profileUrl,
        isFollowing = false
    )
}

fun Comment.toCommentItem(): CommentItem {
    return CommentItem(
        commentId = commentId,
        userId = user.id,
        userName = user.name,
        userProfileImageUrl = user.profileUrl,
        content = content,
        timeAt = timeAt.toRelativeTime(),
        replyCount = replyCount,
        likeCount = likeCount
    )
}