package com.dhkim.home

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.LikeFeed
import com.dhkim.domain.feed.model.UploadState
import com.dhkim.feed.common.CommentItem
import com.dhkim.feed.common.FeedCommentBottomSheet
import com.dhkim.feed.common.FeedContent
import com.dhkim.feed.common.FeedItem
import com.dhkim.feed.common.FeedItemType
import com.dhkim.feed.common.FeedLikerBottomSheet
import com.dhkim.feed.common.FollowingFeedOptionBottomSheet
import com.dhkim.feed.common.MyFeedOptionBottomSheet
import com.dhkim.feed.common.ReplyGroup
import com.dhkim.feed.common.SponsoredFeedOptionBottomSheet
import com.dhkim.feed.common.SuggestedFeedOptionBottomSheet
import com.dhkim.feed.common.Timestamp
import com.dhkim.feed.common.UserItem
import com.dhkim.feed.common.toFeedItem
import com.dhkim.ui.shimmerEffect
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    feedState: LazyListState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    feeds: LazyPagingItems<FeedItem>,
    likers: LazyPagingItems<UserItem>,
    comments: LazyPagingItems<CommentItem>,
    replies: ImmutableList<ReplyGroup>,
    onAction: (HomeAction) -> Unit,
    onFeedLayoutChange: (Boolean) -> Unit,
) {
    val feedUploadStatuses = uiState.feedUploadStatuses
    val likeFeeds = uiState.likeFeeds
    val menuVisibleFeed = uiState.menuVisibleFeed
    val commentBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val bottomSheetState = bottomSheetScaffoldState.bottomSheetState
    val scope = rememberCoroutineScope()
    val onNotInterestedClick: () -> Unit = remember(menuVisibleFeed) {
        {
            menuVisibleFeed?.feedId?.let {
                onAction(HomeAction.HideFeed(feedId = it))
            }
            scope.launch {
                bottomSheetState.hide()
            }
        }
    }

    BottomSheetScaffold(
        sheetPeekHeight = 0.dp,
        sheetContainerColor = InstagramTheme.colors.background,
        scaffoldState = bottomSheetScaffoldState,
        sheetDragHandle = { if (menuVisibleFeed == null) null else BottomSheetDefaults.DragHandle() },
        sheetSwipeEnabled = bottomSheetScaffoldState.bottomSheetState.currentValue != SheetValue.Hidden,
        sheetContent = {
            if (menuVisibleFeed == null) return@BottomSheetScaffold

            when (menuVisibleFeed.type) {
                is FeedItemType.Mine -> {
                    MyFeedOptionBottomSheet(
                        isLikeCountVisible = menuVisibleFeed.isLikeCountVisible,
                        isCommentEnabled = menuVisibleFeed.isCommentEnabled,
                        onLikeVisibleChange = { onAction(HomeAction.ToggleLikeCountVisibility) },
                        onCommentEnabledChange = { onAction(HomeAction.ToggleEnableComment) },
                        onEditClick = {},
                        onDeleteClick = {}
                    )
                }

                is FeedItemType.Following -> {
                    FollowingFeedOptionBottomSheet(
                        isFollowing = true,
                        onFollowChanged = {},
                        onNotInterestedClick = onNotInterestedClick,
                        onAccountInfoClick = {}
                    )
                }

                is FeedItemType.Suggested -> {
                    SuggestedFeedOptionBottomSheet(
                        onNotInterestedClick = onNotInterestedClick,
                        onAccountInfoClick = {}
                    )
                }

                is FeedItemType.Sponsored -> {
                    SponsoredFeedOptionBottomSheet(
                        onNotInterestedClick = onNotInterestedClick,
                        onAccountInfoClick = {}
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }
                    }
                }
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onAction(HomeAction.RefreshFeeds) },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    state = feedState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .onGloballyPositioned {
                            onFeedLayoutChange(it.size != IntSize.Zero)
                        }
                ) {
                    items(
                        count = feeds.itemCount + feedUploadStatuses.size,
                        key = { index ->
                            if (index < feedUploadStatuses.size) {
                                feedUploadStatuses[index].feedId
                            } else {
                                feeds.itemKey { it.feedId }.invoke(index - feedUploadStatuses.size)
                            }
                        },
                        contentType = { index ->
                            if (index < feedUploadStatuses.size) {
                                "upload_status"
                            } else {
                                feeds.itemContentType { "feed_item" }.invoke(index - feedUploadStatuses.size)
                            }
                        }
                    ) { index ->
                        if (index < feedUploadStatuses.size) {
                            FeedUploadStatusContent(feedUploadStatus = feedUploadStatuses[index])
                        } else {
                            feeds[index - feedUploadStatuses.size]?.let { item ->
                                val isLiked = likeFeeds.any { it.feedId == item.feedId }
                                val feedItem = item.copy(isLiked = isLiked)

                                FeedContent(
                                    feedItem = feedItem,
                                    onLikeClick = { onAction(HomeAction.ToggleLike(feedItem.feedId)) },
                                    onLikersClick = { onAction(HomeAction.ShowLikers(feedItem)) },
                                    onCommentClick = { onAction(HomeAction.ShowComments(feedItem)) },
                                    onProfileClick = { },
                                    onMoreClick = {
                                        onAction(HomeAction.ShowFeedMenu(feedItem))
                                        scope.launch {
                                            bottomSheetState.expand()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when {
        uiState.shouldShowLikersBottomSheet -> {
            ModalBottomSheet(
                containerColor = InstagramTheme.colors.background,
                onDismissRequest = { onAction(HomeAction.DismissBottomSheet) },
                modifier = Modifier
                    .padding(
                        bottom = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
            ) {
                FeedLikerBottomSheet(
                    users = likers
                )
            }
        }

        uiState.shouldShowCommentsBottomSheet -> {
            ModalBottomSheet(
                containerColor = InstagramTheme.colors.background,
                sheetState = commentBottomSheetState,
                onDismissRequest = { onAction(HomeAction.DismissBottomSheet) },
                modifier = Modifier
                    .padding(
                        top = WindowInsets.statusBars
                            .asPaddingValues()
                            .calculateTopPadding()
                    )
                    .fillMaxHeight()
            ) {
                FeedCommentBottomSheet(
                    userProfileImageUrl = uiState.userProfileImageUrl,
                    comments = comments,
                    replies = replies,
                    addComment = { comment ->
                        onAction(HomeAction.AddComment(comment))
                    },
                    addReply = { commentItem, content ->
                        onAction(HomeAction.ReplyComment(comment = commentItem, content = content.value))
                    },
                    showReplies = { comment ->
                        onAction(HomeAction.ShowReplies(comment))
                    }
                )
            }
        }
    }
}

@Composable
fun FeedUploadStatusContent(feedUploadStatus: FeedUploadStatus) {
    val thumbnail = feedUploadStatus.thumbnail
    val bitmap = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.size)
    val uploadStatusText = when (feedUploadStatus.uploadState) {
        UploadState.LOADING,
        UploadState.IMAGE_SUCCESS -> stringResource(R.string.feed_upload_status_loading)

        UploadState.IDLE,
        UploadState.FAIL -> stringResource(R.string.feed_upload_status_fail)

        UploadState.COMPLETE -> stringResource(R.string.feed_upload_status_success)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 10.dp)
    ) {
        GlideImage(
            imageModel = { bitmap },
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect()
                )
            },
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
            previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
            modifier = Modifier
                .width(48.dp)
                .aspectRatio(1f)
        )

        Text(
            text = uploadStatusText,
            style = InstagramTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@HomeScreenPreviews
@Composable
private fun HomeScreenPreview() {
    val mockFeedsFlow = flowOf(
        PagingData.from(
            listOf(
                Feed(
                    feedId = "1",
                    userId = "user1",
                    userName = "Tester 1",
                    userProfileImage = "",
                    imageUrls = listOf("https://picsum.photos/400/400"),
                    caption = "Test Caption 1",
                    timestamp = System.currentTimeMillis(),
                    likeCount = 10,
                    commentCount = 5
                ),
                Feed(
                    feedId = "2",
                    userId = "user2",
                    userName = "Tester 2",
                    userProfileImage = "",
                    imageUrls = listOf("https://picsum.photos/400/400"),
                    caption = "Test Caption 2",
                    timestamp = System.currentTimeMillis(),
                    likeCount = 20,
                    commentCount = 10
                ),
                Feed(
                    feedId = "3",
                    userId = "user2",
                    userName = "Tester 2",
                    userProfileImage = "",
                    imageUrls = listOf("https://picsum.photos/400/400"),
                    caption = "Test Caption 2",
                    timestamp = System.currentTimeMillis(),
                    likeCount = 20,
                    commentCount = 10
                )
            ).map { it.toFeedItem(myUserId = "user2") }
        )
    )

    val mockFeeds = mockFeedsFlow.collectAsLazyPagingItems()
    val feedUploadStatuses = mutableListOf<FeedUploadStatus>().apply {
        repeat(1) {
            add(
                FeedUploadStatus(
                    feedId = "feedId_$it",
                    thumbnail = ByteArray(size = 10),
                    imageUrls = listOf("https://picsum.photos/400/400"),
                    uploadState = UploadState.COMPLETE,
                    shouldUpload = true
                )
            )
        }
    }.toImmutableList()

    val users = mutableListOf<UserItem>().apply {
        repeat(10) {
            add(
                UserItem(
                    id = "userId$it",
                    name = "Tester$it",
                    profileImageUrl = "",
                    isFollowing = false
                )
            )
        }
    }
    val mockUsersFlow = flowOf(PagingData.from(users))
    val likers = mockUsersFlow.collectAsLazyPagingItems()

    val commentItems = mutableListOf<CommentItem>().apply {
        repeat(10) {
            add(
                CommentItem(
                    commentId = "id$it",
                    userId = "userId$it",
                    userName = "Tester$it",
                    userProfileImageUrl = "",
                    content = "Test Comment $it",
                    timeAt = Timestamp.JustNow,
                    replyCount = if (it % 2 == 0) 3 else 0,
                    likeCount = if (it % 2 == 0) 10 else 3
                )
            )
        }
    }
    val mockCommentsFlow = flowOf(PagingData.from(commentItems))
    val comments = mockCommentsFlow.collectAsLazyPagingItems()

    val uiState = HomeUiState(
        feedUploadStatuses = feedUploadStatuses,
        likeFeeds = persistentSetOf(LikeFeed("1", "user1")),
        menuVisibleFeed = null,
        isNetworkAvailable = true
    )

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                uiState = uiState,
                feedState = rememberLazyListState(),
                bottomSheetScaffoldState = bottomSheetScaffoldState,
                feeds = mockFeeds,
                likers = likers,
                comments = comments,
                replies = persistentListOf(),
                onAction = {},
                onFeedLayoutChange = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class HomeScreenPreviews