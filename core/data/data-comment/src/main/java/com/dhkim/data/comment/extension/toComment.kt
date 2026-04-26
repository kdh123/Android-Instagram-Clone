package com.dhkim.data.comment.extension

import com.dhkim.data.comment.model.CommentDto
import com.dhkim.data.comment.model.ReplyDto
import com.dhkim.data.user.extension.toUser
import com.dhkim.data.user.extension.toUserDto
import com.dhkim.domain.comment.model.Comment
import com.dhkim.domain.comment.model.Reply

fun CommentDto.toComment(): Comment {
    return Comment(
        commentId = commentId,
        targetId = targetId,
        user = user.toUser(),
        content = content,
        timeAt = timeAt,
        replyCount = replyCount,
        likeCount = likeCount
    )
}

fun ReplyDto.toReply(): Reply {
    return Reply(
        replyId = replyId,
        commentId = commentId,
        user = user.toUser(),
        content = content,
        timeAt = timeAt,
        likeCount = likeCount
    )
}

fun Reply.toDto(): ReplyDto {
    return ReplyDto(
        replyId = replyId,
        commentId = commentId,
        user = user.toUserDto(),
        content = content,
        timeAt = timeAt,
        likeCount = likeCount
    )
}