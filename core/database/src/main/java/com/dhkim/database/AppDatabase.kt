package com.dhkim.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dhkim.database.dao.FeedUploadDao
import com.dhkim.database.dao.HiddenFeedDao
import com.dhkim.database.entity.FeedUploadStatusEntity
import com.dhkim.database.entity.HiddenFeedEntity

private const val DATABASE_VERSION = 1

@Database(
    entities = [
        FeedUploadStatusEntity::class,
        HiddenFeedEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feedUploadDao(): FeedUploadDao
    abstract fun hiddenFeedDao(): HiddenFeedDao
}