package com.dhkim.data.feed.repository

import androidx.paging.PagingData
import androidx.paging.map
import com.dhkim.data.feed.dataSource.FeedLocalDataSource
import com.dhkim.data.feed.dataSource.FeedRemoteDataSource
import com.dhkim.data.feed.extension.toEntity
import com.dhkim.data.feed.extension.toFeedUploadStatus
import com.dhkim.data.feed.extension.toHiddenFeed
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource
) : FeedRepository {

    override fun getFeeds(): Flow<PagingData<Feed>> {
        return remoteDataSource.getFeeds(pageSize = 10).map { feeds ->
            feeds.map { it.toFeed() }
        }
    }

    override fun uploadFeed(feed: Feed): Flow<Unit> {
        return remoteDataSource.uploadFeed(feed)
    }

    override fun uploadImage(storagePath: String, file: File): Flow<String> {
        return remoteDataSource.uploadImage(storagePath, file)
    }

    override fun getFeedUploadStatuses(): Flow<List<FeedUploadStatus>> {
        return localDataSource.getFeedUploadStatuses().map { statuses ->
            statuses.map { it.toFeedUploadStatus() }
        }
    }

    override fun getFeedUploadStatus(feedId: String): Flow<FeedUploadStatus?> {
        return localDataSource.getFeedUploadStatus(feedId).map { it?.toFeedUploadStatus() }
    }

    override suspend fun insertFeedUploadStatus(feedUploadStatus: FeedUploadStatus) {
        localDataSource.insertFeedUploadStatus(feedUploadStatus.toEntity())
    }

    override suspend fun deleteFeedUploadStatus(feedId: String) {
        localDataSource.deleteFeedUploadStatus(feedId)
    }

    override suspend fun clearFeedUploadStatuses() {
        localDataSource.clearFeedUploadStatuses()
    }

    override fun getHiddenFeeds(): Flow<Set<HiddenFeed>> {
        return localDataSource.getHiddenFeeds().map { hiddenFeeds ->
            hiddenFeeds.map { it.toHiddenFeed() }.toSet()
        }
    }

    override fun isHidden(feedId: String): Flow<Boolean> {
        return localDataSource.isHidden(feedId)
    }

    override suspend fun hideFeed(userId: String, hiddenFeed: HiddenFeed) {
        localDataSource.insertHiddenFeed(hiddenFeed.toEntity())
        remoteDataSource.hideFeed(userId, hiddenFeed.feedId).first()
    }

    override suspend fun unhideFeed(userId: String, feedId: String) {
        localDataSource.unhideFeed(feedId)
        remoteDataSource.unhideFeed(userId, feedId).first()
    }

    override suspend fun clearHiddenFeeds() {
        localDataSource.clearHiddenFeeds()
    }
}