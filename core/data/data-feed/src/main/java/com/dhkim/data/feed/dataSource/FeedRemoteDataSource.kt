package com.dhkim.data.feed.dataSource

import androidx.paging.PagingData
import com.dhkim.data.feed.model.toDto
import com.dhkim.domain.feed.model.Feed
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FeedRemoteDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val feedRef = database.getReference("feeds")

    fun getFeeds(): Flow<PagingData<Feed>> {
        return flowOf()
    }

    fun uploadFeed(feed: Feed) {
        feedRef.child(feed.feedId).setValue(feed.toDto())
    }
}