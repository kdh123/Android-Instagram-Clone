package com.dhkim.database.di

import android.content.Context
import androidx.room.Room
import com.dhkim.database.AppDatabase
import com.dhkim.database.dao.FeedDao
import com.dhkim.database.dao.FeedUploadDao
import com.dhkim.database.dao.HiddenFeedDao
import com.dhkim.database.dao.LikeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "instagram_clone.db"
        ).build()
    }

    @Provides
    fun provideFeedUploadDao(database: AppDatabase): FeedUploadDao {
        return database.feedUploadDao()
    }

    @Provides
    fun provideHiddenFeedDao(database: AppDatabase): HiddenFeedDao {
        return database.hiddenFeedDao()
    }

    @Provides
    fun provideFeedDao(database: AppDatabase): FeedDao {
        return database.feedDao()
    }

    @Provides
    fun provideLikeDao(database: AppDatabase): LikeDao {
        return database.likeDao()
    }
}