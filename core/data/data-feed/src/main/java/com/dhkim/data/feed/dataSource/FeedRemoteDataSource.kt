package com.dhkim.data.feed.dataSource

import androidx.core.net.toUri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.common.retryWithDelay
import com.dhkim.data.feed.model.CommentDto
import com.dhkim.data.feed.model.HiddenFeedDto
import com.dhkim.data.feed.model.LikeFeedDto
import com.dhkim.data.feed.model.ReplyDto
import com.dhkim.data.feed.model.UserDto
import com.dhkim.data.feed.model.toDto
import com.dhkim.database.AppDatabase
import com.dhkim.database.entity.HomeFeedEntity
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class FeedRemoteDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseDatabase: FirebaseDatabase,
    private val storage: FirebaseStorage
) {
    private val feedRef = firebaseDatabase.getReference("feeds")
    private val likeRef = firebaseDatabase.getReference("likes")
    private val userRef = firebaseDatabase.getReference("users")
    private val hiddenFeedRef = firebaseDatabase.getReference("hidden_feeds")
    private val commentRef = firebaseDatabase.getReference("comments")
    private val replyRef = firebaseDatabase.getReference("replies")
    private val storageRef = storage.reference

    fun getHomeFeed(): Flow<PagingData<HomeFeedEntity>> = Pager(
        config = PagingConfig(pageSize = 30),
        remoteMediator = HomeFeedRemoteMediator(feedRef.child("feeds_by_feed_id"), appDatabase),
        pagingSourceFactory = { appDatabase.feedDao().getHomeFeeds() }
    ).flow

    fun uploadFeed(feed: Feed, userId: String): Flow<Unit> {
        return flow {
            val feedUpdates = hashMapOf<String, Any?>(
                "/feeds_by_user/$userId/${feed.feedId}" to feed.toDto(),
                "/feeds_by_feed_id/${feed.feedId}" to feed.toDto(),
            )
            feedRef.updateChildren(feedUpdates).await()
            emit(Unit)
        }.retryWithDelay()
    }

    fun uploadImage(storagePath: String, file: File): Flow<String> {
        return flow {
            val imageRef = storageRef.child(storagePath)
            imageRef.putFile(file.toUri()).await()
            val downloadUrl = "${imageRef.downloadUrl.await()}"
            emit(downloadUrl)
        }.retryWithDelay()
    }

    suspend fun getHiddenFeeds(userId: String): List<HiddenFeedDto> {
        val snapshot = hiddenFeedRef.child(userId).get().await()
        val hiddenFeeds = snapshot.children
            .mapNotNull { it.getValue(HiddenFeedDto::class.java) }
        return hiddenFeeds
    }

    fun hideFeed(userId: String, feedId: String): Flow<Unit> {
        return flow {
            val hiddenFeedRef = hiddenFeedRef.child(userId).child(feedId)
            val dto = HiddenFeedDto(feedId = feedId)
            hiddenFeedRef.setValue(dto).await()
            emit(Unit)
        }
    }

    fun unhideFeed(userId: String, feedId: String): Flow<Unit> {
        return flow {
            val hiddenFeedRef = hiddenFeedRef.child(userId).child(feedId)
            hiddenFeedRef.child(feedId).removeValue().await()
            emit(Unit)
        }
    }

    suspend fun toggleLikeCountVisibility(feedId: String, userId: String, isVisible: Boolean) {
        val feedUpdates = hashMapOf<String, Any?>(
            "/feeds_by_feed_id/$feedId/showLikeCount" to isVisible,
            "/feeds_by_user/$userId/$feedId/showLikeCount" to isVisible
        )
        feedRef.updateChildren(feedUpdates).await()
    }

    suspend fun toggleLike(feedId: String, userDto: UserDto, isLiked: Boolean): Boolean {
        val userId = userDto.id
        val likeUpdates = hashMapOf<String, Any?>(
            "/likes_by_feed/$feedId/$userId" to if (isLiked) userDto else null,
            "/likes_by_user/$userId/$feedId" to if (isLiked) System.currentTimeMillis() else null
        )

        likeRef.updateChildren(likeUpdates).await()

        val nextLikerSnapshot = likeRef.child("likes_by_feed").child(feedId)
            .orderByValue()
            .limitToLast(2)
            .get().await()

        val nextLikerId = nextLikerSnapshot.children
            .map { it.key }
            .firstOrNull()

        val feedsUpdates = if (nextLikerId != null) {
            val nextLikerName = userRef.child(nextLikerId).child("name").get().await().value as String
            hashMapOf<String, Any?>(
                "feeds_by_feed_id/$feedId/representativeLikerId" to nextLikerId,
                "feeds_by_feed_id/$feedId/representativeLikerName" to nextLikerName,
                "feeds_by_user/$userId/$feedId/representativeLikerId" to nextLikerId,
                "feeds_by_user/$userId/$feedId/representativeLikerName" to nextLikerName
            )
        } else {
            hashMapOf<String, Any?>(
                "feeds_by_feed_id/$feedId/representativeLikerId" to null,
                "feeds_by_feed_id/$feedId/representativeLikerName" to null,
                "feeds_by_user/$userId/$feedId/representativeLikerId" to null,
                "feeds_by_user/$userId/$feedId/representativeLikerName" to null
            )
        }

        feedRef.updateChildren(feedsUpdates).await()

        return if (isLiked) {
            incrementLikeCount(feedId, userId).first()
        } else {
            decrementLikeCount(feedId, userId).first()
        }
    }

    private fun incrementLikeCount(feedId: String, userId: String): Flow<Boolean> {
        return callbackFlow {
            feedRef.child("feeds_by_feed_id").child(feedId).child("likeCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val nextValue = currentValue + 1
                    val updates = hashMapOf<String, Any?>(
                        "feeds_by_feed_id/$feedId/likeCount" to nextValue,
                        "feeds_by_user/$userId/$feedId/likeCount" to nextValue
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

    private fun decrementLikeCount(feedId: String, userId: String): Flow<Boolean> {
        return callbackFlow {
            val mainCountRef = feedRef.child("feeds_by_feed_id").child(feedId).child("likeCount")

            mainCountRef.get().addOnSuccessListener { snapshot ->
                val currentValue = snapshot.getValue(Int::class.java) ?: 0
                val newValue = if (currentValue > 0) currentValue - 1 else 0
                val updates = hashMapOf<String, Any?>(
                    "feeds_by_feed_id/$feedId/likeCount" to newValue,
                    "feeds_by_user/$userId/$feedId/likeCount" to newValue
                )

                feedRef.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(true)
                    } else {
                        trySend(false)
                    }
                }
            }.addOnFailureListener {
                trySend(false)
            }

            awaitClose()
        }
    }

    suspend fun getLikesByFeed(feedId: String): List<LikeFeedDto> {
        return try {
            val snapshot = likeRef.child("likes_by_feed").child(feedId).get().await()
            snapshot.children.mapNotNull { child ->
                val userId = child.key ?: return@mapNotNull null
                val timestamp = child.getValue(Long::class.java) ?: 0L
                LikeFeedDto(
                    feedId = feedId,
                    userId = userId,
                    isLikeAt = timestamp
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLikesByUser(userId: String): List<LikeFeedDto> {
        return try {
            val snapshot = likeRef.child("likes_by_user").child(userId).get().await()
            snapshot.children.mapNotNull { child ->
                val feedId = child.key ?: return@mapNotNull null
                val timestamp = child.getValue(Long::class.java) ?: 0L
                LikeFeedDto(
                    feedId = feedId,
                    userId = userId,
                    isLikeAt = timestamp
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun toggleEnableComment(feedId: String, userId: String, isEnabled: Boolean) {
        val feedUpdates = hashMapOf<String, Any?>(
            "/feeds_by_user/$userId/$feedId/commentEnabled" to isEnabled,
            "/feeds_by_feed_id/$feedId/commentEnabled" to isEnabled,
        )

        feedRef.updateChildren(feedUpdates).await()
    }

    fun getLikeUsers(feedId: String): Flow<PagingData<UserDto>> {
        val query = likeRef.child("likes_by_feed").child(feedId)
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { LikeUsersPagingSource(query, 20) }
        ).flow
    }

    fun getComments(feedId: String): Flow<PagingData<CommentDto>> {
        val query = commentRef.child(feedId)
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { CommentPagingSource(query, 20) }
        ).flow
    }

    suspend fun addComment(feedId: String, userDto: UserDto, content: String): CommentDto {
        val commentDto = CommentDto(
            targetId = feedId,
            user = userDto,
            content = content
        )

        incrementCommentCount(feedId).first()
        commentRef.child(feedId).child(commentDto.commentId).setValue(commentDto).await()

        return commentDto
    }

    @JvmInline
    value class RemainingReplyCount(val value: Int)

    suspend fun deleteComment(feedId: String, commentId: String): RemainingReplyCount {
        val replyCountRef = commentRef.child(feedId).child(commentId).child("replyCount")
        val currentReplyCount = try {
            val snapshot = replyCountRef.get().await()
            snapshot.getValue(Int::class.java) ?: 0
        } catch (_: Exception) {
            0
        }

        commentRef.child(feedId).child(commentId).setValue(null).await()
        replyRef.child(commentId).setValue(null).await()
        val remainingReplyCount = decrementCommentCount(feedId, currentReplyCount).first()
        return remainingReplyCount
    }

    private fun incrementCommentCount(feedId: String): Flow<Boolean> {
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

    private fun decrementCommentCount(feedId: String, currentReplyCount: Int): Flow<RemainingReplyCount> {
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

    suspend fun replyComment(
        feedId: String,
        commentId: String,
        user: UserDto,
        comment: String
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
        incrementReplyCount(feedId, commentId).first()
        incrementCommentCount(feedId).first()

        return reply
    }

    suspend fun deleteReply(feedId: String, commentId: String, replyId: String): ReplyDto? {
        val deletedReply = replyRef.child(commentId).child(replyId).get().await().getValue(ReplyDto::class.java)
        replyRef.child(commentId).child(replyId).setValue(null).await()
        decrementReplyCount(feedId, commentId).first()
        decrementCommentCount(feedId, 0).first()

        return deletedReply
    }

    private fun incrementReplyCount(feedId: String, commentId: String): Flow<Boolean> {
        return callbackFlow {
            commentRef.child(feedId).child(commentId).child("replyCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val nextValue = currentValue + 1
                    val updates = hashMapOf<String, Any?>(
                        "$feedId/$commentId/replyCount" to nextValue,
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

    private fun decrementReplyCount(feedId: String, commentId: String): Flow<Boolean> {
        return callbackFlow {
            commentRef.child(feedId).child(commentId).child("replyCount")
                .get().addOnSuccessListener { snapshot ->
                    val currentValue = snapshot.getValue(Int::class.java) ?: 0
                    val nextValue = (currentValue - 1).let {
                        if (it < 0) 0 else it
                    }
                    val updates = hashMapOf<String, Any?>(
                        "$feedId/$commentId/replyCount" to nextValue,
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