package com.dhkim.domain.reels.repository

import com.dhkim.domain.reels.model.Reels
import kotlinx.coroutines.flow.Flow

interface ReelsRepository {

    fun getReels(): Flow<List<Reels>>
    suspend fun prefetchVideo(url: String)
}