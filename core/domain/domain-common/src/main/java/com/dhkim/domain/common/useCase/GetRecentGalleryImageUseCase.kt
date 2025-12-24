package com.dhkim.domain.common.useCase

import com.dhkim.domain.common.model.GalleryImage
import com.dhkim.domain.common.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentGalleryImageUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository
) {

    operator fun invoke(): Flow<GalleryImage?> {
        return galleryRepository.getRecentGalleryImageUseCase()
    }
}