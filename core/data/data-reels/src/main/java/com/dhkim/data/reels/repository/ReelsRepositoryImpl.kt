package com.dhkim.data.reels.repository

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.dhkim.data.reels.dataSource.ReelsRemoteDataSource
import com.dhkim.domain.reels.model.Reel
import com.dhkim.domain.reels.repository.ReelsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReelsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReelsRemoteDataSource
) : ReelsRepository {

    override fun getReels(): Flow<List<Reel>> {
        return remoteDataSource.getReels().map { it.map { it.toReels() } }
    }

    @OptIn(UnstableApi::class)
    override suspend fun prefetchVideo(url: String) {
        remoteDataSource.prefetchVideo(url)
    }
}