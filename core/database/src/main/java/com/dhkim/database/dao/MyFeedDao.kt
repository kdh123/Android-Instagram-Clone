package com.dhkim.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhkim.database.entity.MyFeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyFeedDao {

    @Query("SELECT * FROM my_feeds ORDER BY timestamp DESC")
    fun getMyFeedsPagingSource(): PagingSource<Int, MyFeedEntity>

    @Query("SELECT * FROM my_feeds ORDER BY timestamp DESC")
    fun getMyFeeds(): Flow<List<MyFeedEntity>>

    @Query("SELECT * FROM my_feeds WHERE feedId = :feedId")
    fun getMyFeed(feedId: String): Flow<MyFeedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyFeeds(feeds: List<MyFeedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMyFeed(feed: MyFeedEntity)

    @Query("DELETE FROM my_feeds WHERE feedId = :feedId")
    suspend fun deleteMyFeed(feedId: String)

    @Query("DELETE FROM my_feeds")
    suspend fun clearMyFeeds()
}