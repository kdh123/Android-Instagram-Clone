package com.dhkim.domain.feed.repository

import androidx.paging.PagingData
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface FeedRepository {

    fun getFeeds(): Flow<PagingData<Feed>>
    fun uploadFeed(feed: Feed): Flow<Unit>
    fun uploadImage(storagePath: String, file: File): Flow<String>
    fun getFeedUploadStatuses(): Flow<List<FeedUploadStatus>>
    fun getFeedUploadStatus(feedId: String): Flow<FeedUploadStatus?>
    suspend fun insertFeedUploadStatus(feedUploadStatus: FeedUploadStatus)
    suspend fun deleteFeedUploadStatus(feedId: String)
}