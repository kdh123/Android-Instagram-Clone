package com.dhkim.video.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideVideoCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "reels_media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(100L * 1024 * 1024)
        return SimpleCache(cacheDir, evictor, StandaloneDatabaseProvider(context))
    }

    // 추가: Prefetcher나 Repository에서 직접 사용할 수 있도록 Factory 제공
    @Provides
    @Singleton
    @androidx.media3.common.util.UnstableApi
    fun provideCacheDataSourceFactory(
        cache: Cache
    ): CacheDataSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    // 기존: ExoPlayer 빌더에 넣을 Factory
    @Provides
    @Singleton
    @androidx.media3.common.util.UnstableApi
    fun provideMediaSourceFactory(
        cacheDataSourceFactory: CacheDataSource.Factory
    ): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(cacheDataSourceFactory)
    }
}