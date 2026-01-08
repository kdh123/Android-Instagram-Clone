package com.dhkim.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhkim.database.entity.HomeFeedRemoteKey

@Dao
interface HomeFeedRemoteKeyDao {

    @Query("SELECT * FROM home_feed_remote_keys ORDER BY feedId DESC LIMIT 1")
    suspend fun getLastKey(): HomeFeedRemoteKey?

    @Query("DELETE FROM home_feed_remote_keys")
    suspend fun clearRemoteKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<HomeFeedRemoteKey>)
}
