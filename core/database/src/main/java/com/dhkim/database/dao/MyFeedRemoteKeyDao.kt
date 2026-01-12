package com.dhkim.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhkim.database.entity.MyFeedRemoteKey

@Dao
interface MyFeedRemoteKeyDao {

    @Query("SELECT * FROM my_feed_remote_keys ORDER BY feedId DESC LIMIT 1")
    suspend fun getLastKey(): MyFeedRemoteKey?

    @Query("DELETE FROM my_feed_remote_keys")
    suspend fun clearRemoteKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<MyFeedRemoteKey>)
}
