package com.dhkim.domain.reels.useCase

import com.dhkim.domain.reels.repository.ReelsRepository
import javax.inject.Inject

class PrefetchReelsUseCase @Inject constructor(
    private val reelsRepository: ReelsRepository
) {

    suspend operator fun invoke(url: String) {
        reelsRepository.prefetchVideo(url)
    }
}