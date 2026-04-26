package com.dhkim.data.comment.dataSource

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.data.comment.model.CommentDto
import com.dhkim.data.comment.model.ReplyDto
import com.dhkim.data.user.model.UserDto
import com.dhkim.domain.comment.model.CommentType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CommentRemoteDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
) {
    private val commentRef = firebaseDatabase.getReference("comments")
    private val replyRef = firebaseDatabase.getReference("replies")
    private val feedRef = firebaseDatabase.getReference("feeds")
    private val reelRef = firebaseDatabase.getReference("reels")

    fun getComments(targetId: String): Flow<PagingData<CommentDto>> {
        val dbRef = commentRef.child(targetId)
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { CommentPagingSource(dbRef, 20) }
        ).flow
    }

    private suspend fun addComment(targetId: String, userDto: UserDto, content: String, commentType: CommentType): CommentDto {
        val commentDto = CommentDto(
            targetId = targetId,
            user = userDto,
            content = content
        )

        when (commentType) {
            CommentType.FEED -> incrementFeedCommentCount(feedId = targetId).first()
            CommentType.REEL -> incrementReelCommentCount(reelId = targetId).first()
        }

        commentRef.child(targetId).child(commentDto.commentId).setValue(commentDto).await()

        return commentDto
    }

    suspend fun addFeedComment(feedId: String, userDto: UserDto, content: String): CommentDto {
        val commentDto = addComment(
            targetId = feedId,
            userDto = userDto,
            content = content,
            commentType = CommentType.FEED
        )

        return commentDto
    }

    suspend fun addReelComment(reelId: String, userDto: UserDto, content: String): CommentDto {
        val commentDto = addComment(
            targetId = reelId,
            userDto = userDto,
            content = content,
            commentType = CommentType.REEL
        )

        return commentDto
    }

    @JvmInline
    value class RemainingReplyCount(val value: Int)

    private suspend fun deleteComment(feedId: String, commentId: String, commentType: CommentType): RemainingReplyCount {
        val replyCountRef = commentRef.child(feedId).child(commentId).child("replyCount")
        val currentReplyCount = try {
            val snapshot = replyCountRef.get().await()
            snapshot.getValue(Int::class.java) ?: 0
        } catch (_: Exception) {
            0
        }

        commentRef.child(feedId).child(commentId).setValue(null).await()
        replyRef.child(commentId).setValue(null).await()
        val remainingReplyCount = when (commentType) {
            CommentType.FEED -> decrementFeedCommentCount(feedId, currentReplyCount).first()
            CommentType.REEL -> decrementReelCommentCount(feedId, currentReplyCount).first()
        }
        return remainingReplyCount
    }

    suspend fun deleteFeedComment(feedId: String, commentId: String): RemainingReplyCount {
        val remainingReplyCount = deleteComment(
            feedId = feedId,
            commentId = commentId,
            commentType = CommentType.FEED
        )
        return remainingReplyCount
    }

    suspend fun deleteReelComment(reelId: String, commentId: String): RemainingReplyCount {
        val remainingReplyCount = deleteComment(
            feedId = reelId,
            commentId = commentId,
            commentType = CommentType.REEL
        )
        return remainingReplyCount
    }

    private fun incrementFeedCommentCount(feedId: String): Flow<Boolean> {
        return callbackFlow {
            feedRef.child("feeds_by_feed_id").child(feedId).child("commentCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val nextValue = currentValue + 1
                    val updates = hashMapOf<String, Any?>(
                        "feeds_by_feed_id/$feedId/commentCount" to nextValue,
                    )

                    feedRef.updateChildren(updates).addOnCompleteListener { task ->
                        trySend(task.isSuccessful)
                    }
                }.addOnFailureListener {
                    trySend(false)
                }
            awaitClose()
        }
    }

    private fun decrementFeedCommentCount(feedId: String, currentReplyCount: Int): Flow<RemainingReplyCount> {
        return callbackFlow {
            feedRef.child("feeds_by_feed_id").child(feedId).child("commentCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val newValue = if (currentValue > 0) {
                        currentValue - 1 - currentReplyCount
                    } else {
                        0
                    }
                    val updates = hashMapOf<String, Any?>(
                        "feeds_by_feed_id/$feedId/commentCount" to newValue,
                    )

                    feedRef.updateChildren(updates).addOnCompleteListener { task ->
                        trySend(RemainingReplyCount(value = newValue))
                    }
                }.addOnFailureListener {
                    trySend(RemainingReplyCount(value = currentReplyCount))
                }

            awaitClose()
        }
    }

    private fun incrementReelCommentCount(reelId: String): Flow<Boolean> {
        return callbackFlow {
            reelRef.child("reels_by_reel_id").child(reelId).child("commentCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val nextValue = currentValue + 1
                    val updates = hashMapOf<String, Any?>(
                        "reels_by_reel_id/$reelId/commentCount" to nextValue,
                    )

                    reelRef.updateChildren(updates).addOnCompleteListener { task ->
                        trySend(task.isSuccessful)
                    }
                }.addOnFailureListener {
                    trySend(false)
                }
            awaitClose()
        }
    }

    private fun decrementReelCommentCount(reelId: String, currentReplyCount: Int): Flow<RemainingReplyCount> {
        return callbackFlow {
            reelRef.child("reels_by_reel_id").child(reelId).child("commentCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val newValue = if (currentValue > 0) {
                        currentValue - 1 - currentReplyCount
                    } else {
                        0
                    }
                    val updates = hashMapOf<String, Any?>(
                        "reels_by_reel_id/$reelId/commentCount" to newValue,
                    )

                    reelRef.updateChildren(updates).addOnCompleteListener { task ->
                        trySend(RemainingReplyCount(value = newValue))
                    }
                }.addOnFailureListener {
                    trySend(RemainingReplyCount(value = currentReplyCount))
                }

            awaitClose()
        }
    }

    fun getReplies(commentId: String): Flow<List<ReplyDto>> {
        return callbackFlow {
            replyRef.child(commentId)
                .orderByKey()
                .limitToLast(10)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val replies = snapshot.children.mapNotNull { it.getValue(ReplyDto::class.java) }
                        trySend(replies)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        trySend(listOf())
                    }
                })
            awaitClose()
        }
    }

    private suspend fun replyComment(
        targetId: String,
        commentId: String,
        user: UserDto,
        comment: String,
        commentType: CommentType
    ): ReplyDto {
        val reply = ReplyDto(
            replyId = "reply_id_${System.currentTimeMillis()}",
            commentId = commentId,
            user = user,
            content = comment,
            timeAt = System.currentTimeMillis(),
            likeCount = 0
        )

        replyRef.child(reply.commentId).child(reply.replyId).setValue(reply).await()
        incrementReplyCount(targetId, commentId).first()

        when (commentType) {
            CommentType.FEED -> incrementFeedCommentCount(targetId).first()
            CommentType.REEL -> incrementReelCommentCount(targetId).first()
        }

        return reply
    }

    suspend fun replyFeedComment(
        feedId: String,
        commentId: String,
        user: UserDto,
        comment: String
    ): ReplyDto {
        val reply = replyComment(
            targetId = feedId,
            commentId = commentId,
            user = user,
            comment = comment,
            commentType = CommentType.FEED
        )

        return reply
    }

    suspend fun replyReelComment(
        reelId: String,
        commentId: String,
        user: UserDto,
        comment: String
    ): ReplyDto {
        val reply = replyComment(
            targetId = reelId,
            commentId = commentId,
            user = user,
            comment = comment,
            commentType = CommentType.REEL
        )

        return reply
    }

    private suspend fun deleteReply(targetId: String, commentId: String, replyId: String, commentType: CommentType): ReplyDto? {
        val deletedReply = replyRef.child(commentId).child(replyId).get().await().getValue(ReplyDto::class.java)
        replyRef.child(commentId).child(replyId).setValue(null).await()
        decrementReplyCount(targetId, commentId).first()
        when (commentType) {
            CommentType.FEED -> decrementFeedCommentCount(targetId, 0).first()
            CommentType.REEL -> decrementReelCommentCount(targetId, 0).first()
        }

        return deletedReply
    }

    suspend fun deleteFeedReply(feedId: String, commentId: String, replyId: String): ReplyDto? {
        val deletedReply = deleteReply(
            targetId = feedId,
            commentId = commentId,
            replyId = replyId,
            commentType = CommentType.FEED
        )

        return deletedReply
    }

    suspend fun deleteReelReply(reelId: String, commentId: String, replyId: String): ReplyDto? {
        val deletedReply = deleteReply(
            targetId = reelId,
            commentId = commentId,
            replyId = replyId,
            commentType = CommentType.REEL
        )

        return deletedReply
    }

    private fun incrementReplyCount(targetId: String, commentId: String): Flow<Boolean> {
        return callbackFlow {
            commentRef.child(targetId).child(commentId).child("replyCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val nextValue = currentValue + 1
                    val updates = hashMapOf<String, Any?>(
                        "$targetId/$commentId/replyCount" to nextValue,
                    )

                    commentRef.updateChildren(updates).addOnCompleteListener { task ->
                        trySend(task.isSuccessful)
                    }
                }.addOnFailureListener {
                    trySend(false)
                }
            awaitClose()
        }
    }

    private fun decrementReplyCount(targetId: String, commentId: String): Flow<Boolean> {
        return callbackFlow {
            commentRef.child(targetId).child(commentId).child("replyCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val nextValue = (currentValue - 1).let {
                        if (it < 0) 0 else it
                    }
                    val updates = hashMapOf<String, Any?>(
                        "$targetId/$commentId/replyCount" to nextValue,
                    )

                    commentRef.updateChildren(updates).addOnCompleteListener { task ->
                        trySend(task.isSuccessful)
                    }
                }.addOnFailureListener {
                    trySend(false)
                }
            awaitClose()
        }
    }
}