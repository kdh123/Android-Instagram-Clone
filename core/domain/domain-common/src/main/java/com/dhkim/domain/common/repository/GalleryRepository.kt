package com.dhkim.domain.common.repository

import androidx.paging.PagingData
import com.dhkim.domain.common.model.GalleryImage
import kotlinx.coroutines.flow.Flow

interface GalleryRepository {

    fun getRecentGalleryImageUseCase(): Flow<GalleryImage?>
    fun getGalleryImages(pageSize: Int): Flow<PagingData<GalleryImage>>
}