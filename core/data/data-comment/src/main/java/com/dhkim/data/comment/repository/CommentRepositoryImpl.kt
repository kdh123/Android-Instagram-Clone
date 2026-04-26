package com.dhkim.data.comment.repository

import androidx.paging.PagingData
import androidx.paging.map
import com.dhkim.data.comment.dataSource.CommentRemoteDataSource
import com.dhkim.data.comment.extension.toComment
import com.dhkim.data.comment.extension.toReply
import com.dhkim.data.user.extension.toUserDto
import com.dhkim.domain.comment.model.Comment
import com.dhkim.domain.comment.model.CommentType
import com.dhkim.domain.comment.model.Reply
import com.dhkim.domain.comment.repository.CommentRepository
import com.dhkim.domain.user.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val remoteDataSource: CommentRemoteDataSource,
) : CommentRepository {

    override fun getComments(targetId: String): Flow<PagingData<Comment>> {
        return remoteDataSource.getComments(targetId).map { pagingData ->
            pagingData.map { it.toComment() }
        }
    }

    override suspend fun addComment(targetId: String, user: User, comment: String, commentType: CommentType): Comment {
        val addedComment = when (commentType) {
            CommentType.FEED -> remoteDataSource.addFeedComment(targetId, user.toUserDto(), comment).toComment()
            CommentType.REEL -> remoteDataSource.addReelComment(targetId, user.toUserDto(), comment).toComment()
        }

        return addedComment
    }

    override suspend fun deleteComment(targetId: String, commentId: String, commentType: CommentType): Int {
        val remainingReplyCount = when (commentType) {
            CommentType.FEED -> remoteDataSource.deleteFeedComment(targetId, commentId).value
            CommentType.REEL -> remoteDataSource.deleteReelComment(targetId, commentId).value
        }
        return remainingReplyCount
    }

    override fun getReplies(commentId: String): Flow<List<Reply>> {
        return remoteDataSource.getReplies(commentId).map { replies ->
            replies.map { it.toReply() }
        }
    }

    override suspend fun replyComment(
        targetId: String,
        commentId: String,
        user: User,
        comment: String,
        commentType: CommentType
    ): Reply {
        val addedReply = when (commentType) {
            CommentType.FEED -> remoteDataSource.replyFeedComment(targetId, commentId, user.toUserDto(), comment).toReply()
            CommentType.REEL -> remoteDataSource.replyReelComment(targetId, commentId, user.toUserDto(), comment).toReply()
        }

        return addedReply
    }

    override suspend fun deleteReply(targetId: String, commentId: String, replyId: String, commentType: CommentType): Reply? {
        val deletedReply = when (commentType) {
            CommentType.FEED -> remoteDataSource.deleteFeedReply(targetId, commentId, replyId)?.toReply()
            CommentType.REEL -> remoteDataSource.deleteReelReply(targetId, commentId, replyId)?.toReply()
        }

        return deletedReply
    }
}