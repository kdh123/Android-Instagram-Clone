package com.dhkim.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhkim.database.entity.FeedUploadStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedUploadDao {

    @Query("SELECT * FROM feed_upload_status")
    fun getAllUploadStatuses(): Flow<List<FeedUploadStatusEntity>>

    @Query("SELECT * FROM feed_upload_status WHERE feedId = :feedId")
    fun getUploadStatus(feedId: String): Flow<FeedUploadStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(feedUpload: FeedUploadStatusEntity)

    @Query("DELETE FROM feed_upload_status WHERE feedId = :feedId")
    suspend fun deleteStatus(feedId: String)

    @Query("DELETE FROM feed_upload_status")
    suspend fun clear()
}