package com.dhkim.data.feed.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LikeFeedDto(
    val feedId: String = "",
    val userId: String = "",
    val isLikeAt: Long = 0L
)