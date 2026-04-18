package com.dhkim.data.reels.model

import com.dhkim.domain.reels.model.Reels

data class ReelsDto(
    private val id: String,
    private val url: String
) {
    fun toReels(): Reels {
        return Reels(
            id = id,
            url = url
        )
    }
}
