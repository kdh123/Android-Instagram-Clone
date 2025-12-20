package com.dhkim.instagramclone.di

import com.dhkim.instagramclone.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Named("WEB_CLIENT_ID")
    fun provideWebClientId(): String {
        return BuildConfig.WEB_CLIENT_ID
    }
}