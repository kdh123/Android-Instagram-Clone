package com.dhkim.database.entity

import androidx.room.Entity

@Entity(
    tableName = "liked_feeds",
    primaryKeys = ["feedId", "userId"]
)
data class LikeEntity(
    val feedId: String,
    val userId: String,
    val likedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)