package com.dhkim.data.common.dataSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhkim.domain.common.model.GalleryImage

class GalleryPagingSource(
    private val galleryDataSource: GalleryDataSource,
    private val pageSize: Int
) : PagingSource<Int, GalleryImage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryImage> {
        return try {
            val currentOffset = params.key ?: 0
            val response = galleryDataSource.fetchImages(
                limit = params.loadSize,
                offset = currentOffset
            )

            val prevKey = if (currentOffset == 0) null else currentOffset - pageSize
            val nextKey = if (response.size < params.loadSize) null else currentOffset + response.size

            LoadResult.Page(
                data = response,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GalleryImage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(pageSize) ?: anchorPage?.nextKey?.minus(pageSize)
        }
    }
}