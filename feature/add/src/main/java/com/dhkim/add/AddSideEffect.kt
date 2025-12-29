package com.dhkim.add

sealed interface AddSideEffect {

    data class ShowToast(val message: String) : AddSideEffect
    data object NavigateToHome : AddSideEffect
    data object NavigateToFeedUpload : AddSideEffect
    data class ScrollToItem(val imageUri: String?) : AddSideEffect
}