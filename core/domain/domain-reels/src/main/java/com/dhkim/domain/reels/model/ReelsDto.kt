package com.dhkim.domain.reels.model

data class Reel(
    val id: String,
    val url: String,
    val playbackPosition: Long = 0,
    val isLiked: Boolean = false
)