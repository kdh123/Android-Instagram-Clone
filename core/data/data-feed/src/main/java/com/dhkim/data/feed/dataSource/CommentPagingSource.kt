package com.dhkim.data.feed.dataSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhkim.data.feed.model.CommentDto
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class CommentPagingSource(
    private val query: DatabaseReference,
    private val pageSize: Int
) : PagingSource<String, CommentDto>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CommentDto> {
        return try {
            val lastKey = params.key

            val currentQuery = if (lastKey == null) {
                query.orderByKey().limitToFirst(pageSize)
            } else {
                query.orderByKey().startAfter(lastKey).limitToFirst(pageSize)
            }

            val snapshot = currentQuery.get().await()
            val comments = snapshot.children
                .mapNotNull { it.getValue(CommentDto::class.java) }

            val nextKey = if (comments.size < pageSize) null else comments.last().commentId

            LoadResult.Page(
                data = comments.reversed(),
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, CommentDto>): String? {
        return null
    }
}