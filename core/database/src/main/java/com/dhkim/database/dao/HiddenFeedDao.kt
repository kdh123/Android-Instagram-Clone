package com.dhkim.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhkim.database.entity.HiddenFeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenFeedDao {

    @Query("SELECT * FROM hidden_feeds ORDER BY hiddenAt DESC")
    fun getAllHiddenFeeds(): Flow<List<HiddenFeedEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM hidden_feeds WHERE feedId = :feedId)")
    fun isHidden(feedId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHiddenFeed(hiddenFeeds: List<HiddenFeedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHiddenFeed(hiddenFeed: HiddenFeedEntity)

    @Query("DELETE FROM hidden_feeds WHERE feedId = :feedId")
    suspend fun unhideFeed(feedId: String)

    @Query("DELETE FROM hidden_feeds")
    suspend fun clear()
}