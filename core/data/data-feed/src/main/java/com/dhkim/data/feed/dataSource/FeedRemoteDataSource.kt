package com.dhkim.data.feed.dataSource

import androidx.core.net.toUri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.common.retryWithDelay
import com.dhkim.data.feed.model.HiddenFeedDto
import com.dhkim.data.feed.model.toDto
import com.dhkim.database.AppDatabase
import com.dhkim.database.entity.HomeFeedEntity
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
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
    private val hiddenFeedRef = firebaseDatabase.getReference("hidden_feeds")
    private val storageRef = storage.reference

    fun getHomeFeed(): Flow<PagingData<HomeFeedEntity>> = Pager(
        config = PagingConfig(pageSize = 10),
        remoteMediator = HomeFeedRemoteMediator(feedRef, appDatabase),
        pagingSourceFactory = { appDatabase.feedDao().getHomeFeeds() }
    ).flow

    fun uploadFeed(feed: Feed): Flow<Unit> {
        return flow {
            val feedRef = feedRef.child(feed.feedId)
            feedRef.setValue(feed.toDto()).await()
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

    fun updateLikeCountVisibility(feedId: String, shouldShow: Boolean): Flow<Unit> {
        return flow {
            val feedRef = feedRef.child(feedId).child("shouldShowLikeCount")
            feedRef.setValue(shouldShow).await()
            emit(Unit)
        }.retryWithDelay()
    }

    fun updateCommentVisibility(feedId: String, shouldShow: Boolean): Flow<Unit> {
        return flow {
            val feedRef = feedRef.child(feedId).child("enableComment")
            feedRef.setValue(shouldShow).await()
            emit(Unit)
        }.retryWithDelay()
    }
}