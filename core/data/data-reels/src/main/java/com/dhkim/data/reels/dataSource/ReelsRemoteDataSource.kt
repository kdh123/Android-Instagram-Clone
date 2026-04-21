package com.dhkim.data.reels.dataSource

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @param:ApplicationContext private val context: Context
) {

    private val storageRef = storage.reference

    fun getReels(): Flow<List<ReelsDto>> {
        return flow {
            val reelsNames = listOf("sample1.mp4", "sample2.mp4", "sample4.mp4")
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
    @androidx.annotation.OptIn(UnstableApi::class)
    suspend fun prefetchVideo(url: String) {
        withContext(Dispatchers.IO) {
            try {
                val uri = url.toUri()
                // 첫 2MB만 다운로드하도록 DataSpec 설정 (length 지정)
                val dataSpec = DataSpec.Builder()
                    .setUri(uri)
                    .setPosition(0)
                    .setLength(2 * 1024 * 1024) // 2MB
                    .build()

                val dataSource = cacheDataSourceFactory.createDataSource()

                val cacheWriter = CacheWriter(
                    dataSource, // CacheDataSource 인스턴스
                    dataSpec,        // 다운로드할 범위가 지정된 DataSpec
                    null,            // temporaryBuffer (null로 두면 내부적으로 기본 버퍼 128KB를 자동 생성해서 사용함)
                    null             // progressListener (진척도 콜백이 필요 없다면 null)
                )

                // 캐싱 실행 (이미 캐시되어 있다면 즉시 종료됨)
                cacheWriter.cache()
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                e.printStackTrace()
            }
        }
    }
}