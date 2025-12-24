package com.dhkim.add

import com.dhkim.domain.feed.model.Feed

sealed interface AddAction {

    data class UploadFeed(
        val feed: Feed,
        val imageUrls: List<String>
    ) : AddAction

    data class SelectImage(
        val imageUri: String
    ) : AddAction
}