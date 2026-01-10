package com.dhkim.home

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
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
import com.dhkim.feed.common.FeedContent
import com.dhkim.feed.common.FeedItem
import com.dhkim.feed.common.FeedItemType
import com.dhkim.feed.common.FollowingFeedBottomSheet
import com.dhkim.feed.common.MyFeedBottomSheet
import com.dhkim.feed.common.SponsoredFeedBottomSheet
import com.dhkim.feed.common.SuggestedFeedBottomSheet
import com.dhkim.feed.common.toFeedItem
import com.dhkim.ui.NoticeMessage
import com.dhkim.ui.shimmerEffect
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    feedState: LazyListState,
    feedUploadStatuses: ImmutableList<FeedUploadStatus>,
    feeds: LazyPagingItems<FeedItem>,
    likeFeeds: Set<LikeFeed>,
    menuVisibleFeed: FeedItem?,
    isNetworkAvailable: Boolean,
    onAction: (HomeAction) -> Unit,
    onFeedLayoutChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var showNoticeMessage: String? by remember { mutableStateOf(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pagingLoading = feeds.loadState.refresh is LoadState.Loading
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
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

    LaunchedEffect(bottomSheetScaffoldState) {
        snapshotFlow { bottomSheetScaffoldState.bottomSheetState.currentValue }
            .collect { currentValue ->
                if (currentValue == SheetValue.Hidden || currentValue == SheetValue.PartiallyExpanded) {
                    onAction(HomeAction.DismissFeedMenu)
                }
            }
    }

    LaunchedEffect(pagingLoading) {
        if (!pagingLoading) isRefreshing = false
    }

    LaunchedEffect(feeds.loadState.refresh) {
        if (feeds.loadState.refresh is LoadState.NotLoading) {
            delay(300)
            feedState.scrollToItem(0)
        }
    }

    LaunchedEffect(showNoticeMessage) {
        if (showNoticeMessage != null) {
            delay(3_000)
            showNoticeMessage = null
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
                    MyFeedBottomSheet(
                        isLikeCountVisible = menuVisibleFeed.isLikeCountVisible,
                        isCommentEnabled = menuVisibleFeed.isCommentEnabled,
                        onLikeVisibleChange = { onAction(HomeAction.UpdateLikeCountVisibility(isVisible = it)) },
                        onCommentEnabledChange = { onAction(HomeAction.UpdateEnableComment(isEnabled = it)) },
                        onEditClick = {},
                        onDeleteClick = {}
                    )
                }

                is FeedItemType.Following -> {
                    FollowingFeedBottomSheet(
                        isFollowing = true,
                        onFollowChanged = {},
                        onNotInterestedClick = onNotInterestedClick,
                        onAccountInfoClick = {}
                    )
                }

                is FeedItemType.Suggested -> {
                    SuggestedFeedBottomSheet(
                        onNotInterestedClick = onNotInterestedClick,
                        onAccountInfoClick = {}
                    )
                }

                is FeedItemType.Sponsored -> {
                    SponsoredFeedBottomSheet(
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
                isRefreshing = isRefreshing,
                onRefresh = {
                    if (isNetworkAvailable) {
                        isRefreshing = true // 로딩 시작
                        feeds.refresh()
                    } else {
                        isRefreshing = true
                        scope.launch {
                            delay(100)
                            isRefreshing = false
                            showNoticeMessage = context.getString(com.dhkim.feed.common.R.string.feed_refresh_failed)
                        }
                    }
                },
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
                                    onProfileClick = { },
                                    onMoreClick = { feed ->
                                        onAction(HomeAction.ShowFeedMenu(feed))
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

            if (showNoticeMessage != null) {
                NoticeMessage(message = showNoticeMessage!!)
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

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                feedState = rememberLazyListState(),
                feedUploadStatuses = feedUploadStatuses,
                feeds = mockFeeds,
                likeFeeds = setOf(LikeFeed("1", "user1")),
                menuVisibleFeed = null,
                isNetworkAvailable = true,
                onAction = {},
                onFeedLayoutChange = {},
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class HomeScreenPreviews