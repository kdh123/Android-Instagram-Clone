package com.dhkim.data.feed.dataSource

import com.dhkim.database.dao.FeedUploadDao
import com.dhkim.database.entity.FeedUploadStatusEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedLocalDataSource @Inject constructor(
    private val feedUploadDao: FeedUploadDao
) {

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
}