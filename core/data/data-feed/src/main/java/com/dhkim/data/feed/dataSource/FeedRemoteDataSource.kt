package com.dhkim.data.feed.dataSource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.data.feed.model.FeedDto
import com.dhkim.data.feed.model.toDto
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeedRemoteDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {
    private val feedRef = database.getReference("feeds")
    private val storageRef = storage.reference

    fun getFeeds(pageSize: Int): Flow<PagingData<FeedDto>> {
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

    fun uploadImage(filePath: String, byteArray: ByteArray): Flow<String> {
        return flow {
            val imageRef = storageRef.child(filePath)
            imageRef.putBytes(byteArray).await()
            val downloadUrl = "${imageRef.downloadUrl.await()}"
            emit(downloadUrl)
        }
    }
}