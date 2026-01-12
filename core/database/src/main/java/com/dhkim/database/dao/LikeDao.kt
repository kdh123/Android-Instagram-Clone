package com.dhkim.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhkim.database.entity.LikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(likes: List<LikeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Query("DELETE FROM liked_feeds WHERE feedId = :feedId AND userId = :userId")
    suspend fun deleteLike(feedId: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_feeds WHERE feedId = :feedId AND userId = :userId)")
    fun observeIsLiked(feedId: String, userId: String): Flow<Boolean>

    @Query("SELECT * FROM liked_feeds WHERE userId = :userId ORDER BY likedAt DESC")
    fun getAllLikedFeeds(userId: String): Flow<List<LikeEntity>>

    @Query("SELECT * FROM liked_feeds WHERE feedId = :feedId AND userId = :userId")
    fun getLikeFeed(feedId: String, userId: String): Flow<LikeEntity?>

    @Query("SELECT * FROM liked_feeds WHERE isSynced = 0")
    suspend fun getUnSyncedLikes(): List<LikeEntity>
}