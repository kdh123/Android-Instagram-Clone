package com.dhkim.home

sealed interface HomeAction {

    data class HideFeed(val feedId: String) : HomeAction
    data class UnhideFeed(val feedId: String) : HomeAction
}
