package com.dhkim.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhkim.database.entity.HomeFeedEntity
import com.dhkim.database.entity.SearchFeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    @Query("SELECT * FROM home_feeds ORDER BY timestamp DESC")
    fun getHomeFeeds(): PagingSource<Int, HomeFeedEntity>

    @Query("SELECT * FROM home_feeds WHERE feedId = :feedId")
    fun getHomeFeed(feedId: String): Flow<HomeFeedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeFeeds(feeds: List<HomeFeedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHomeFeed(feed: HomeFeedEntity)

    @Query("DELETE FROM home_feeds")
    suspend fun clearHomeFeeds()

    @Query("SELECT * FROM search_feeds ORDER BY timestamp DESC")
    fun getSearchFeeds(): PagingSource<Int, SearchFeedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchFeeds(feeds: List<SearchFeedEntity>)

    @Query("DELETE FROM search_feeds")
    suspend fun clearSearchFeeds()
}