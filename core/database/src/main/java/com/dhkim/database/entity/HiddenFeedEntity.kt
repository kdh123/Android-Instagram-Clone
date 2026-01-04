package com.dhkim.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_feeds")
data class HiddenFeedEntity(
    @PrimaryKey
    val feedId: String,
    val hiddenAt: Long = System.currentTimeMillis()
)