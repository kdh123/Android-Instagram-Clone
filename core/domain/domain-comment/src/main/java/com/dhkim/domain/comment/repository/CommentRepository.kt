package com.dhkim.domain.comment.repository

import androidx.paging.PagingData
import com.dhkim.domain.comment.model.Comment
import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.model.Reply
import com.dhkim.domain.user.model.User
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    fun getComments(targetId: String): Flow<PagingData<Comment>>
    suspend fun addComment(targetId: String, user: User, comment: String, commentType: CommentType): Comment
    suspend fun deleteComment(targetId: String, commentId: String, commentType: CommentType): Int

    fun getReplies(commentId: String): Flow<List<Reply>>
    suspend fun replyComment(targetId: String, commentId: String, user: User, comment: String, commentType: CommentType): Reply
    suspend fun deleteReply(targetId: String, commentId: String, replyId: String, commentType: CommentType): Reply?
}