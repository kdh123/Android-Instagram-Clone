package com.dhkim.domain.reels.useCase

import com.dhkim.domain.reels.model.Reel
import com.dhkim.domain.reels.repository.ReelsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReelsUseCase @Inject constructor(
    private val reelsRepository: ReelsRepository
) {

    operator fun invoke(): Flow<List<Reel>> {
        return reelsRepository.getReels()
    }
}