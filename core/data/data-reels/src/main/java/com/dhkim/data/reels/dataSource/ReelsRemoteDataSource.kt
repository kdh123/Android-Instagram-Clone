package com.dhkim.data.reels.dataSource

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import com.dhkim.common.retryWithDelay
import com.dhkim.data.reels.model.ReelsDto
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReelsRemoteDataSource @OptIn(androidx.media3.common.util.UnstableApi::class)
@Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val cacheDataSourceFactory: CacheDataSource.Factory,
) {

    private val storageRef = storage.reference

    fun getReels(): Flow<List<ReelsDto>> {
        return flow {
            val reelsNames = listOf("sample1.mp4", "sample2.mp4", "sample3.mp4", "sample4.mp4", "sample5.mp4", "sample6.mp4")
            val reelsUrls = reelsNames.map {
                ReelsDto(
                    id = "${System.currentTimeMillis()}",
                    url = "${storageRef.child("reels/$it").downloadUrl.await()}"
                )
            }
            emit(reelsUrls)
        }.retryWithDelay()
    }

    @androidx.media3.common.util.UnstableApi
    @OptIn(UnstableApi::class)
    suspend fun prefetchVideo(url: String) {
        withContext(Dispatchers.IO) {
            try {
                val uri = url.toUri()
                val dataSpec = DataSpec.Builder()
                    .setUri(uri)
                    .setPosition(0)
                    .setLength(2 * 1024 * 1024) // 2MB
                    .build()

                val dataSource = cacheDataSourceFactory.createDataSource()

                val cacheWriter = CacheWriter(
                    dataSource,
                    dataSpec,
                    null,
                    null
                )

                // 캐싱 실행 (이미 캐시되어 있다면 즉시 종료됨)
                cacheWriter.cache()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}