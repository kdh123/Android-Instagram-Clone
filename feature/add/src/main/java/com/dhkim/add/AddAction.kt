package com.dhkim.add

import com.dhkim.domain.feed.model.Feed

sealed interface AddAction {

    data class UploadFeed(val feed: Feed) : AddAction
}