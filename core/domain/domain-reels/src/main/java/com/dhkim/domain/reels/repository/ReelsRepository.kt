package com.dhkim.domain.reels.repository

import com.dhkim.domain.reels.model.Reel
import kotlinx.coroutines.flow.Flow

interface ReelsRepository {

    fun getReels(): Flow<List<Reel>>
    suspend fun prefetchVideo(url: String)
}