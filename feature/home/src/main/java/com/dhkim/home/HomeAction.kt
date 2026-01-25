package com.dhkim.home

import com.dhkim.feed.common.CommentItem
import com.dhkim.feed.common.FeedItem
import com.dhkim.feed.common.ReplyItem

sealed interface HomeAction {

    data object ToggleLikeCountVisibility : HomeAction
    data class HideFeed(val feedId: String) : HomeAction
    data class UnhideFeed(val feedId: String) : HomeAction
    data class ShowFeedMenu(val feed: FeedItem) : HomeAction
    data class ToggleLike(val feedId: String) : HomeAction
    data object ToggleEnableComment : HomeAction
    data object RefreshFeeds : HomeAction
    data object OnFeedCompleted : HomeAction
    data object DismissBottomSheet : HomeAction
    data class ShowLikers(val feedItem: FeedItem) : HomeAction
    data class ShowComments(val feedItem: FeedItem) : HomeAction
    data class AddComment(val comment: String) : HomeAction
    data class DeleteComment(val comment: CommentItem) : HomeAction
    data class ShowReplies(val comment: CommentItem) : HomeAction
    data class ReplyComment(val comment: CommentItem, val content: String) : HomeAction
    data class DeleteReply(val comment: CommentItem, val reply: ReplyItem) : HomeAction
}
