package com.dhkim.feed.common

import androidx.compose.runtime.Stable

@Stable
interface FeedContentScope {
    val feedItem: FeedItem
}

class DefaultFeedContentScope(override val feedItem: FeedItem) : FeedContentScope