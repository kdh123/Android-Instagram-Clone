package com.dhkim.reels

sealed interface ReelsAction {

    data class PrefetchReels(val index: Int) : ReelsAction
}