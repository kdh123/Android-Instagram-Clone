package com.dhkim.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dhkim.database.dao.FeedDao
import com.dhkim.database.dao.FeedUploadDao
import com.dhkim.database.dao.HiddenFeedDao
import com.dhkim.database.dao.HomeFeedRemoteKeyDao
import com.dhkim.database.dao.LikeDao
import com.dhkim.database.dao.MyFeedDao
import com.dhkim.database.dao.MyFeedRemoteKeyDao
import com.dhkim.database.entity.FeedUploadStatusEntity
import com.dhkim.database.entity.HiddenFeedEntity
import com.dhkim.database.entity.HomeFeedEntity
import com.dhkim.database.entity.HomeFeedRemoteKey
import com.dhkim.database.entity.LikeEntity
import com.dhkim.database.entity.MyFeedEntity
import com.dhkim.database.entity.MyFeedRemoteKey
import com.dhkim.database.entity.SearchFeedEntity

private const val DATABASE_VERSION = 1

@Database(
    entities = [
        MyFeedEntity::class,
        MyFeedRemoteKey::class,
        HomeFeedEntity::class,
        HomeFeedRemoteKey::class,
        SearchFeedEntity::class,
        FeedUploadStatusEntity::class,
        HiddenFeedEntity::class,
        LikeEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feedDao(): FeedDao
    abstract fun myFeedDao(): MyFeedDao
    abstract fun feedUploadDao(): FeedUploadDao
    abstract fun hiddenFeedDao(): HiddenFeedDao
    abstract fun homeFeedRemoteKeyDao(): HomeFeedRemoteKeyDao
    abstract fun myFeedRemoteKeyDao(): MyFeedRemoteKeyDao
    abstract fun likeDao(): LikeDao
}