package com.dhkim.data.reels.model

import com.dhkim.domain.reels.model.Reel

data class ReelsDto(
    private val id: String,
    private val url: String
) {
    fun toReels(): Reel {
        return Reel(
            id = id,
            url = url
        )
    }
}
