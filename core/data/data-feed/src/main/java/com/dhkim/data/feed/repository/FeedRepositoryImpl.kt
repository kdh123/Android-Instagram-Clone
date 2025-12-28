package com.dhkim.data.feed.repository

import androidx.paging.PagingData
import androidx.paging.map
import com.dhkim.data.feed.dataSource.FeedRemoteDataSource
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val remoteDataSource: FeedRemoteDataSource
) : FeedRepository {

    override fun getFeeds(): Flow<PagingData<Feed>> {
        return remoteDataSource.getFeeds(pageSize = 10)
            .map { it.map { it.toFeed() } }
    }

    override fun uploadFeed(feed: Feed): Flow<Unit> {
        return remoteDataSource.uploadFeed(feed)
    }

    override fun uploadImage(filePath: String, byteArray: ByteArray): Flow<String> {
        return remoteDataSource.uploadImage(filePath, byteArray)
    }
}