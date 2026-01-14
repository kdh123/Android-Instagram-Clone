package com.dhkim.data.feed.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LikeUserDto(
    val id: String = "",
    val name: String = "",
    val profileUrl: String = ""
)
