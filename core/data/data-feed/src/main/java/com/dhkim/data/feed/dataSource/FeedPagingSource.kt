package com.dhkim.data.feed.dataSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhkim.data.feed.model.FeedDto
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class FeedPagingSource(
    private val query: DatabaseReference,
    private val pageSize: Int
) : PagingSource<String, FeedDto>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, FeedDto> {
        return try {
            val lastKey = params.key

            val currentQuery = if (lastKey == null) {
                query.orderByKey().limitToFirst(pageSize)
            } else {
                query.orderByKey().startAfter(lastKey).limitToFirst(pageSize)
            }

            val snapshot = currentQuery.get().await()
            val feeds = snapshot.children.mapNotNull { it.getValue(FeedDto::class.java) }

            val nextKey = if (feeds.size < pageSize) null else feeds.last().feedId

            LoadResult.Page(
                data = feeds.reversed(),
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, FeedDto>): String? {
        return null
    }
}