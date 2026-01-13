package com.dhkim.home

import com.dhkim.feed.common.FeedItem

sealed interface HomeAction {

    data object ToggleLikeCountVisibility : HomeAction
    data class HideFeed(val feedId: String) : HomeAction
    data class UnhideFeed(val feedId: String) : HomeAction
    data class ShowFeedMenu(val feed: FeedItem) : HomeAction
    data object DismissFeedMenu : HomeAction
    data class ToggleLike(val feedId: String) : HomeAction
    data object ToggleEnableComment : HomeAction
    data object RefreshFeeds : HomeAction
    data object OnFeedCompleted : HomeAction
}
