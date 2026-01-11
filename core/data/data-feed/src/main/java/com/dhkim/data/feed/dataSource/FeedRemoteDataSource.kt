package com.dhkim.data.feed.dataSource

import androidx.core.net.toUri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.common.retryWithDelay
import com.dhkim.data.feed.model.HiddenFeedDto
import com.dhkim.data.feed.model.LikeFeedDto
import com.dhkim.data.feed.model.toDto
import com.dhkim.database.AppDatabase
import com.dhkim.database.entity.HomeFeedEntity
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.FirebaseDatabase
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

    suspend fun toggleLike(feedId: String, myUid: String, isLiked: Boolean): Boolean {
        val likeUpdates = hashMapOf<String, Any?>(
            "/likes_by_feed/$feedId/$myUid" to if (isLiked) System.currentTimeMillis() else null,
            "/likes_by_user/$myUid/$feedId" to if (isLiked) System.currentTimeMillis() else null
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
                "feeds_by_user/$myUid/$feedId/representativeLikerId" to nextLikerId,
                "feeds_by_user/$myUid/$feedId/representativeLikerName" to nextLikerName
            )
        } else {
            hashMapOf<String, Any?>(
                "feeds_by_feed_id/$feedId/representativeLikerId" to null,
                "feeds_by_feed_id/$feedId/representativeLikerName" to null,
                "feeds_by_user/$myUid/$feedId/representativeLikerId" to null,
                "feeds_by_user/$myUid/$feedId/representativeLikerName" to null
            )
        }

        feedRef.updateChildren(feedsUpdates).await()

        return if (isLiked) {
            incrementLikeCount(feedId, myUid).first()
        } else {
            decrementLikeCount(feedId, myUid).first()
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
        } catch (e: Exception) {
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
}