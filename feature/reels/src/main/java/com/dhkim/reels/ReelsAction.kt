package com.dhkim.reels

sealed interface ReelsAction {

    data class ToggleLike(val reelUrl: String): ReelsAction
    data class PrefetchReels(val index: Int) : ReelsAction
    data class SavePlaybackPosition(val reelUrl: String, val position: Long): ReelsAction
}