package com.dhkim.data.feed.repository

import androidx.paging.PagingData
import com.dhkim.data.feed.dataSource.FeedRemoteDataSource
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val remoteDataSource: FeedRemoteDataSource
) : FeedRepository {

    override fun getFeeds(): Flow<PagingData<Feed>> {
        return remoteDataSource.getFeeds(pageSize = 10)
    }

    override fun uploadFeed(feed: Feed) {
        remoteDataSource.uploadFeed(feed)
    }
}