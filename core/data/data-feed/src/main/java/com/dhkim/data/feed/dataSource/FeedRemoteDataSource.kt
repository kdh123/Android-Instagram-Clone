package com.dhkim.data.feed.dataSource

import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.data.feed.model.toDto
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FeedRemoteDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {
    private val feedRef = database.getReference("feeds")
    private val storageRef = storage.reference

    fun getFeeds(pageSize: Int): Flow<PagingData<Feed>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { FeedPagingSource(feedRef, pageSize) }
        ).flow
    }

    fun uploadFeed(feed: Feed): Flow<Unit> {
        return callbackFlow {
            feedRef.child(feed.feedId).setValue(feed.toDto())
                .addOnSuccessListener {
                    trySend(Unit)
                }.addOnFailureListener {
                    close(it)
                }
            awaitClose()
        }
    }

    fun uploadImages(userId: String, images: List<String>): Flow<Unit> {
        return callbackFlow {
            val imageRef = storageRef.child("feeds/${userId}/photo_${System.currentTimeMillis()}.jpg")
            imageRef.putFile(images[0].toUri())
                .addOnSuccessListener {
                    trySend(Unit)
                }.addOnFailureListener {
                    close(it)
                }
            awaitClose()
        }
    }
}