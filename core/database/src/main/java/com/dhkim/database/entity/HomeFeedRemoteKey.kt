package com.dhkim.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_feed_remote_keys")
data class HomeFeedRemoteKey(
    @PrimaryKey val feedId: String,
    val nextKey: String?
)
