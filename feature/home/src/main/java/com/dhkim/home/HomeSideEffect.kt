package com.dhkim.home

sealed interface HomeSideEffect {

    data class ShowToast(val message: String) : HomeSideEffect
}