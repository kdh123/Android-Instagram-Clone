package com.dhkim.add

import com.dhkim.domain.feed.model.Feed

sealed interface AddAction {

    data class SelectImage(
        val imageUri: String
    ) : AddAction

    data object ChangeSelectImageMode: AddAction

    data class UploadFeed(
        val feed: Feed,
        val imageUrls: List<String>
    ) : AddAction
}