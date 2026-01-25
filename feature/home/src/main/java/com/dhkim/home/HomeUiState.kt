package com.dhkim.home

import androidx.compose.runtime.Immutable
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.LikeFeed
import com.dhkim.feed.common.FeedItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class HomeUiState(
    val isRefreshing: Boolean = false,
    val userProfileImageUrl: String = "",
    val myFeeds: ImmutableSet<Feed> = persistentSetOf(),
    val feedUploadStatuses: ImmutableList<FeedUploadStatus> = persistentListOf(),
    val likeFeeds: ImmutableSet<LikeFeed> = persistentSetOf(),
    val targetFeedForLikers: FeedItem? = null,
    val targetFeedForComments: FeedItem? = null,
    val menuVisibleFeed: FeedItem? = null,
    val isNetworkAvailable: Boolean = true
) {
    val showBottomSheet: HomeBottomSheet?
        get() {
            return when {
                menuVisibleFeed != null -> HomeBottomSheet.MENU
                targetFeedForLikers != null -> HomeBottomSheet.LIKERS
                targetFeedForComments != null -> HomeBottomSheet.COMMENTS
                else -> null
            }
        }
}

enum class HomeBottomSheet {
    MENU,
    LIKERS,
    COMMENTS
}
