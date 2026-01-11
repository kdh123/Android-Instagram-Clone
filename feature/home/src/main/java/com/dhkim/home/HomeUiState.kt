package com.dhkim.home

import androidx.compose.runtime.Immutable
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.LikeFeed
import com.dhkim.feed.common.FeedItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class HomeUiState(
    val feedUploadStatuses: ImmutableList<FeedUploadStatus> = persistentListOf(),
    val likeFeeds: ImmutableSet<LikeFeed> = persistentSetOf(),
    val menuVisibleFeed: FeedItem? = null,
    val isNetworkAvailable: Boolean = true
)
