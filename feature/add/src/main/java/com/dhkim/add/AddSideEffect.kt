package com.dhkim.add

sealed interface AddSideEffect {

    data class ShowToast(val message: String) : AddSideEffect
    data class ScrollToItem(val imageUri: String) : AddSideEffect
}