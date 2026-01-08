package com.dhkim.data.feed.dataSource

import com.dhkim.database.dao.FeedDao
import com.dhkim.database.dao.FeedUploadDao
import com.dhkim.database.dao.HiddenFeedDao
import com.dhkim.database.entity.FeedUploadStatusEntity
import com.dhkim.database.entity.HiddenFeedEntity
import com.dhkim.database.entity.HomeFeedEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedLocalDataSource @Inject constructor(
    private val feedDao: FeedDao,
    private val feedUploadDao: FeedUploadDao,
    private val hiddenFeedDao: HiddenFeedDao
) {

    fun getHomeFeed(feedId: String): Flow<HomeFeedEntity> {
        return feedDao.getHomeFeed(feedId)
    }

    suspend fun updateHomeFeed(feed: HomeFeedEntity) {
        feedDao.updateHomeFeed(feed)
    }

    fun getFeedUploadStatuses(): Flow<List<FeedUploadStatusEntity>> {
        return feedUploadDao.getAllUploadStatuses()
    }

    fun getFeedUploadStatus(feedId: String): Flow<FeedUploadStatusEntity?> {
        return feedUploadDao.getUploadStatus(feedId)
    }

    suspend fun insertFeedUploadStatus(feedUploadStatus: FeedUploadStatusEntity) {
        feedUploadDao.insertOrUpdate(feedUploadStatus)
    }

    suspend fun deleteFeedUploadStatus(feedId: String) {
        feedUploadDao.deleteStatus(feedId)
    }

    suspend fun clearFeedUploadStatuses() {
        feedUploadDao.clear()
    }

    fun getHiddenFeeds(): Flow<List<HiddenFeedEntity>> {
        return hiddenFeedDao.getAllHiddenFeeds()
    }

    fun isHidden(feedId: String): Flow<Boolean> {
        return hiddenFeedDao.isHidden(feedId)
    }

    suspend fun insertHiddenFeed(hiddenFeed: HiddenFeedEntity) {
        hiddenFeedDao.insertHiddenFeed(hiddenFeed)
    }

    suspend fun unhideFeed(feedId: String) {
        hiddenFeedDao.unhideFeed(feedId)
    }

    suspend fun clearHiddenFeeds() {
        hiddenFeedDao.clear()
    }
}