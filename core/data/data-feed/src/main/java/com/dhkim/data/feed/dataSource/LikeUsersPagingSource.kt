package com.dhkim.data.feed.dataSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhkim.data.user.model.UserDto
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class LikeUsersPagingSource(
    private val query: DatabaseReference,
    private val pageSize: Int
) : PagingSource<String, UserDto>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, UserDto> {
        return try {
            val lastKey = params.key

            val currentQuery = if (lastKey == null) {
                query.orderByKey().limitToFirst(pageSize)
            } else {
                query.orderByKey().startAfter(lastKey).limitToFirst(pageSize)
            }

            val snapshot = currentQuery.get().await()
            val likeUsers = snapshot.children
                .mapNotNull { it.getValue(UserDto::class.java) }

            val nextKey = if (likeUsers.size < pageSize) null else likeUsers.last().id

            LoadResult.Page(
                data = likeUsers.reversed(),
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, UserDto>): String? {
        return null
    }
}