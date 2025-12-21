package com.dhkim.data.feed.dataSource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.data.feed.model.toDto
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedRemoteDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val feedRef = database.getReference("feeds")

    fun getFeeds(pageSize: Int): Flow<PagingData<Feed>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { FeedPagingSource(feedRef, pageSize) }
        ).flow
    }

    fun uploadFeed(feed: Feed) {
        feedRef.child(feed.feedId).setValue(feed.toDto())
    }
}