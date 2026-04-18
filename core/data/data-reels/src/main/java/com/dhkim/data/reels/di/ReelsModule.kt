package com.dhkim.data.reels.di

import com.dhkim.data.reels.repository.ReelsRepositoryImpl
import com.dhkim.domain.reels.repository.ReelsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReelsModule {

    @Binds
    @Singleton
    abstract fun bindReelsRepository(reelsRepositoryImpl: ReelsRepositoryImpl): ReelsRepository
}