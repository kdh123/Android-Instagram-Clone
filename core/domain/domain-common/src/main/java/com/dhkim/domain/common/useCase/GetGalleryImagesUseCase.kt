package com.dhkim.domain.common.useCase

import androidx.paging.PagingData
import com.dhkim.domain.common.model.GalleryImage
import com.dhkim.domain.common.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGalleryImagesUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {

    operator fun invoke(pageSize: Int): Flow<PagingData<GalleryImage>> {
        return galleryRepository.getGalleryImages(pageSize)
    }
}