package com.dhkim.data.feed.dataSource

import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.data.feed.model.FeedDto
import com.dhkim.data.feed.model.HiddenFeedDto
import com.dhkim.data.feed.model.toDto
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class FeedRemoteDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {
    private val feedRef = database.getReference("feeds")
    private val hiddenFeedRef = database.getReference("hidden_feeds")
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
        return flow {
            val feedRef = feedRef.child(feed.feedId)
            feedRef.setValue(feed.toDto()).await()
            emit(Unit)
        }.retryWhen { _, attempt ->
            if (attempt < 3) {
                val nextDelay = (attempt + 1) * 1_000L
                delay(nextDelay)
                true
            } else {
                false
            }
        }
    }

    fun uploadImage(storagePath: String, file: File): Flow<String> {
        return flow {
            val imageRef = storageRef.child(storagePath)
            imageRef.putFile(file.toUri()).await()
            val downloadUrl = "${imageRef.downloadUrl.await()}"
            emit(downloadUrl)
        }.retryWhen { _, attempt ->
            if (attempt < 3) {
                val nextDelay = (attempt + 1) * 1_000L
                delay(nextDelay)
                true
            } else {
                false
            }
        }
    }

    fun getHiddenFeeds(userId: String): Flow<List<HiddenFeedDto>> {
        return flow {
            val snapshot = hiddenFeedRef.child(userId).get().await()
            val hiddenFeeds = snapshot.children
                .mapNotNull { it.getValue(HiddenFeedDto::class.java) }
            emit(hiddenFeeds)
        }
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
}