package com.dhkim.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_feeds")
data class SearchFeedEntity(
    @PrimaryKey val feedId: String,
    val type: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String,
    val imageUrls: List<String> = listOf(),
    val caption: String,
    val timestamp: Long,
    val likeCount: Int,
    val commentCount: Int,
    val adUrl: String,
    val isLikeCountVisible: Boolean,
    val isCommentEnabled: Boolean,
)