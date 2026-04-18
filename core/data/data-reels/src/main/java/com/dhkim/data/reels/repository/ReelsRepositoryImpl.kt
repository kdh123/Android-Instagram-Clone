package com.dhkim.data.reels.repository

import com.dhkim.data.reels.dataSource.ReelsRemoteDataSource
import com.dhkim.domain.reels.model.Reels
import com.dhkim.domain.reels.repository.ReelsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReelsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReelsRemoteDataSource
) : ReelsRepository {

    override fun getReels(): Flow<List<Reels>> {
        return remoteDataSource.getReels().map { it.map { it.toReels() } }
    }
}