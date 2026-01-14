package com.dhkim.domain.feed.repository

import androidx.paging.PagingData
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.model.LikeFeed
import com.dhkim.domain.feed.model.LikeUser
import com.dhkim.domain.user.model.User
import kotlinx.coroutines.flow.Flow
import java.io.File

interface FeedRepository {

    fun getMyFeeds(): Flow<Set<Feed>>
    fun getHomeFeeds(): Flow<PagingData<Feed>>
    fun uploadFeed(feed: Feed, userId: String): Flow<Unit>
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
    suspend fun toggleLikeCountVisibility(feedId: String, userId: String, isVisible: Boolean)
    suspend fun toggleLike(feedId: String, userId: String, userName: String, isLiked: Boolean)
    suspend fun remoteToggleLike(feedId: String, user: User)
    suspend fun syncLikeFeeds(userId: String)
    fun getAllLikedFeeds(userId: String): Flow<Set<LikeFeed>>
    fun getLikeFeed(feedId: String, userId: String): Flow<LikeFeed?>
    suspend fun updateLikeFeed(likeFeed: LikeFeed)
    suspend fun getUnSyncedLikes(): List<LikeFeed>
    suspend fun toggleEnableComment(feedId: String, userId: String, isEnabled: Boolean)
    fun getLikeUsers(feedId: String): Flow<PagingData<LikeUser>>
}