package com.dhkim.data.feed.dataSource

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dhkim.data.feed.extension.toSearchEntity
import com.dhkim.data.feed.model.FeedDto
import com.dhkim.database.AppDatabase
import com.dhkim.database.entity.SearchFeedEntity
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPagingApi::class)
class SearchFeedRemoteMediator(
    private val query: DatabaseReference,
    private val database: AppDatabase
) : RemoteMediator<Int, SearchFeedEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, SearchFeedEntity>): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.APPEND -> state.lastItemOrNull()?.feedId ?: return MediatorResult.Success(false)
                LoadType.PREPEND -> return MediatorResult.Success(true)
            }

            val snapshot = if (loadKey == null) {
                query.limitToFirst(state.config.pageSize).get().await()
            } else {
                query.startAfter(loadKey).limitToFirst(state.config.pageSize).get().await()
            }

            val entities = snapshot.children.mapNotNull { 
                it.getValue(FeedDto::class.java)?.toSearchEntity()
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) database.feedDao().clearSearchFeeds()
                database.feedDao().insertSearchFeeds(entities)
            }

            MediatorResult.Success(endOfPaginationReached = entities.isEmpty())
        } catch (e: Exception) { MediatorResult.Error(e) }
    }
}