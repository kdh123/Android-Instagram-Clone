package com.dhkim.data.common.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhkim.data.common.dataSource.GalleryDataSource
import com.dhkim.data.common.dataSource.GalleryPagingSource
import com.dhkim.domain.common.model.GalleryImage
import com.dhkim.domain.common.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GalleryRepositoryImpl @Inject constructor(
    private val galleryDataSource: GalleryDataSource
) : GalleryRepository {

    override fun getGalleryImages(pageSize: Int): Flow<PagingData<GalleryImage>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize
            ),
            pagingSourceFactory = { GalleryPagingSource(galleryDataSource, pageSize) }
        ).flow
    }
}