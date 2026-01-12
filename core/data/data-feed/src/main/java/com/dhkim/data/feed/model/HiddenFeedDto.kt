package com.dhkim.data.feed.model

import com.dhkim.domain.feed.model.HiddenFeed
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class HiddenFeedDto(
    val feedId: String = "",
    val hiddenAt: Long = 0L
) {
    fun toHiddenFeed(): HiddenFeed {
        return HiddenFeed(
            feedId = feedId,
            hiddenAt = hiddenAt
        )
    }
}

fun HiddenFeed.toDto(): HiddenFeedDto {
    return HiddenFeedDto(
        feedId = feedId,
        hiddenAt = hiddenAt
    )
}
