package com.dhkim.data.feed.repository

import androidx.paging.PagingData
import com.dhkim.data.feed.dataSource.FeedRemoteDataSource
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val remoteDataSource: FeedRemoteDataSource
) : FeedRepository {

    override fun getFeeds(): Flow<PagingData<Feed>> {
        return flowOf()
    }

    override fun uploadFeed(feed: Feed) {
        remoteDataSource.uploadFeed(feed)
    }
}