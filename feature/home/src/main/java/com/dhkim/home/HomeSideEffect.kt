package com.dhkim.home

sealed interface HomeSideEffect {

    data object ShowRefreshFeedsFailNotice : HomeSideEffect
    data class ShowToast(val message: String) : HomeSideEffect
}