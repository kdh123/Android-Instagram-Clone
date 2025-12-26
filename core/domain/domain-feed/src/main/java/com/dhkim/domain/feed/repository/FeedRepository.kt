package com.dhkim.domain.feed.repository

import androidx.paging.PagingData
import com.dhkim.domain.feed.model.Feed
import kotlinx.coroutines.flow.Flow

interface FeedRepository {

    fun getFeeds(): Flow<PagingData<Feed>>
    fun uploadFeed(feed: Feed): Flow<Unit>
    fun uploadImages(filePath: String, imageUrl: String): Flow<Unit>
}