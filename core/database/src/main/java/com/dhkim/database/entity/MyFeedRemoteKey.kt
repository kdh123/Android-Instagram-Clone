package com.dhkim.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_feed_remote_keys")
data class MyFeedRemoteKey(
    @PrimaryKey val feedId: String,
    val nextKey: String?
)
