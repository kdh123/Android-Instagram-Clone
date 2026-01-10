package com.dhkim.domain.feed.repository

import androidx.paging.PagingData
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.model.LikeFeed
import kotlinx.coroutines.flow.Flow
import java.io.File

interface FeedRepository {

    fun getHomeFeeds(): Flow<PagingData<Feed>>
    fun uploadFeed(feed: Feed): Flow<Unit>
    fun uploadImage(storagePath: String, file: File): Flow<String>
    fun getFeedUploadStatuses(): Flow<List<FeedUploadStatus>>
    fun getFeedUploadStatus(feedId: String): Flow<FeedUploadStatus?>
    suspend fun insertFeedUploadStatus(feedUploadStatus: FeedUploadStatus)
    suspend fun deleteFeedUploadStatus(feedId: String)
    suspend fun clearFeedUploadStatuses()
    fun getHiddenFeeds(): Flow<Set<HiddenFeed>>
    fun isHidden(feedId: String): Flow<Boolean>
    suspend fun syncHiddenFeeds(userId: String)
    suspend fun hideFeed(userId:String, hiddenFeed: HiddenFeed)
    suspend fun unhideFeed(userId:String, feedId: String)
    suspend fun clearHiddenFeeds()
    suspend fun updateLikeCountVisibility(feedId: String, isVisible: Boolean)
    suspend fun updateCommentVisibility(feedId: String, enableComment: Boolean)
    suspend fun toggleLike(feedId: String, userId: String)
    suspend fun remoteToggleLike(feedId: String, userId: String)
    suspend fun syncLikeFeeds(userId: String)
    fun getAllLikedFeeds(userId: String): Flow<Set<LikeFeed>>
    fun getLikeFeed(feedId: String, userId: String): Flow<LikeFeed?>
    suspend fun updateLikeFeed(likeFeed: LikeFeed)
    suspend fun getUnSyncedLikes(): List<LikeFeed>
}