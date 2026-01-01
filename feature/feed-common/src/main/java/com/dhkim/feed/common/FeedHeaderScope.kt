package com.dhkim.feed.common

import androidx.compose.runtime.Stable

@Stable
interface FeedHeaderScope {
    val feed: FeedItem
}

class DefaultFeedHeaderScope(override val feed: FeedItem) : FeedHeaderScope