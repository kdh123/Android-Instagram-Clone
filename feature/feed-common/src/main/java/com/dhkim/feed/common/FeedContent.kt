package com.dhkim.feed.common

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.noRippleClick
import com.dhkim.ui.shimmerEffect
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

@Composable
fun FeedContent(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    when (feedItem.type) {
        is FeedItemType.Mine,
        is FeedItemType.Following,
        is FeedItemType.Suggested -> {
            CommonFeedContent(
                feedItem = feedItem,
                onProfileClick = onProfileClick,
                onMoreClick = onMoreClick
            )
        }

        is FeedItemType.Sponsored -> {
            SponsoredFeedContent(
                feedItem = feedItem,
                onProfileClick = onProfileClick,
                onMoreClick = onMoreClick
            )
        }
    }
}

@Composable
fun CommonFeedContent(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    FeedContents(
        feedItem = feedItem,
        onProfileClick = onProfileClick,
        onMoreClick = onMoreClick,
    ) {
        FeedImagePager()
        FeedItemActions(
            onLikeClick = { },
            onCommentClick = { },
            onShareClick = { }
        )
        FeedCaption(
            onUserClick = { },
        )
        FeedTimestamp()
    }
}

@Composable
fun SponsoredFeedContent(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    FeedContents(
        feedItem = feedItem,
        onProfileClick = onProfileClick,
        onMoreClick = onMoreClick,
    ) {
        SponsoredFeedImage()
        FeedItemActions(
            onLikeClick = { },
            onCommentClick = { },
            onShareClick = { }
        )
        FeedCaption(
            onUserClick = { },
        )
    }
}

@Composable
fun FeedContents(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit,
    content: @Composable FeedContentScope.() -> Unit
) {
    val scope = remember(feedItem) { DefaultFeedContentScope(feedItem) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        FeedHeader(
            feedItem = feedItem,
            onProfileClick = onProfileClick,
            onMoreClick = onMoreClick
        )

        scope.content()
    }
}

@Composable
fun FeedContentScope.FeedTimestamp() {
    val context = LocalContext.current
    val timestamp = when (val feedType = feedItem.type) {
        is FeedItemType.Mine -> feedType.timestamp
        is FeedItemType.Following -> feedType.timestamp
        is FeedItemType.Suggested -> feedType.timestamp
        is FeedItemType.Sponsored -> return
    }
    val timestampText = when (timestamp) {
        is Timestamp.JustNow -> stringResource(R.string.time_just_now)
        is Timestamp.MinutesAgo -> context.getString(R.string.time_minutes_ago, timestamp.minutes)
        is Timestamp.HoursAgo -> context.getString(R.string.time_hours_ago, timestamp.hours)
        is Timestamp.DaysAgo -> context.getString(R.string.time_days_ago, timestamp.days)
        is Timestamp.Date -> timestamp.date
    }

    Text(
        text = timestampText,
        style = InstagramTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
fun FeedContentScope.FeedCaption(
    onUserClick: (String) -> Unit
) {
    val userName = feedItem.userName
    val caption = feedItem.caption
    var isExpanded by remember { mutableStateOf(false) }
    var lastCharIndex by remember { mutableStateOf(0) }
    val moreText = stringResource(R.string.feed_more)
    val fullText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
            pushStringAnnotation(tag = "USER", annotation = userName)
            append(userName)
            pop()
        }
        append(" ")
        append(caption)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        val finalText = remember(isExpanded, lastCharIndex, fullText) {
            if (isExpanded) {
                fullText
            } else {
                if (lastCharIndex > 0 && lastCharIndex < fullText.length) {
                    buildAnnotatedString {
                        append(fullText.subSequence(0, lastCharIndex))
                        withStyle(style = SpanStyle(color = Color.Gray, fontSize = 14.sp)) {
                            pushStringAnnotation(tag = "MORE", annotation = "more")
                            append(moreText)
                            pop()
                        }
                    }
                } else {
                    fullText
                }
            }
        }

        ClickableText(
            text = finalText,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 20.sp
            ),
            onClick = { offset ->
                finalText.getStringAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                    when (annotation.tag) {
                        "USER" -> onUserClick(userName)
                        "MORE" -> isExpanded = true
                    }
                }
            },
            onTextLayout = { textLayoutResult ->
                if (!isExpanded && textLayoutResult.lineCount > 2) {
                    val lineEndIndex = textLayoutResult.getLineEnd(1, visibleEnd = true)
                    val adjustIndex = (lineEndIndex - moreText.length - 5).coerceAtLeast(0)
                    if (lastCharIndex != adjustIndex) {
                        lastCharIndex = adjustIndex
                    }
                }
            },
            modifier = Modifier
                .padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun FeedContentScope.FeedItemActions(
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val isLiked = feedItem.isLiked
    val commentCount = feedItem.commentCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) Color.Red else InstagramTheme.colors.onBackground,
            modifier = Modifier
                .noRippleClick(onClick = onLikeClick)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_comment),
                contentDescription = "Comment",
                modifier = Modifier
                    .padding(end = 4.dp)
                    .noRippleClick(onClick = onCommentClick)
            )

            Text(
                text = "$commentCount",
                style = InstagramTheme.typography.bodyMedium,
                modifier = Modifier

            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Send,
            contentDescription = "Share",
            modifier = Modifier
                .noRippleClick(onClick = onShareClick)
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.ic_bookmark),
            contentDescription = "Comment",
        )
    }
}

@Composable
fun FeedContentScope.SponsoredFeedImage() {
    if (feedItem.type !is FeedItemType.Sponsored) return

    val imageUrl = (feedItem.type as FeedItemType.Sponsored).imageUrl

    Column {
        GlideImage(
            imageModel = { imageUrl },
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect()
                )
            },
            failure = {
                FeedImageLoadFailContent()
            },
            previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Text(
            text = stringResource(R.string.feed_view_details),
            style = InstagramTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.DarkGray)
                .padding(10.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedContentScope.FeedImagePager() {
    val imageUrls = when (val feedType = feedItem.type) {
        is FeedItemType.Mine -> feedType.imageUrls
        is FeedItemType.Following -> feedType.imageUrls
        is FeedItemType.Suggested -> feedType.imageUrls
        is FeedItemType.Sponsored -> return
    }

    val pagerState = rememberPagerState { imageUrls.size }

    Box {
        HorizontalPager(
            state = pagerState,
            key = { imageUrls[it] },
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            GlideImage(
                imageModel = { imageUrls[pageIndex] },
                imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmerEffect()
                    )
                },
                failure = {
                    FeedImageLoadFailContent()
                },
                previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }

        if (imageUrls.size > 1) {
            PageIndicator(
                count = imageUrls.size,
                currentIndex = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        PageNumberIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
fun PageNumberIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    var shouldShowPageNumberIndicator by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            shouldShowPageNumberIndicator = true
            delay(3_000)
            shouldShowPageNumberIndicator = false
        }
    }

    AnimatedVisibility(
        visible = shouldShowPageNumberIndicator,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .padding(12.dp)
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = CircleShape,
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun PageIndicator(
    count: Int,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(count) { index ->
            val color = if (currentIndex == index) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.5f)
            }
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun FeedHeader(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    when (feedItem.type) {
        is FeedItemType.Mine -> {
            MyFeedHeader(
                feedItem = feedItem,
                onProfileClick = onProfileClick,
                onMoreClick = onMoreClick
            )
        }

        is FeedItemType.Following -> {
            FollowingFeedHeader(
                feedItem = feedItem,
                onProfileClick = onProfileClick,
                onMoreClick = onMoreClick
            )
        }

        is FeedItemType.Suggested -> {
            SuggestedFeedHeader(
                feedItem = feedItem,
                onProfileClick = onProfileClick,
                onMoreClick = onMoreClick
            )
        }

        is FeedItemType.Sponsored -> {
            SponsoredFeedHeader(
                feedItem = feedItem,
                onProfileClick = onProfileClick,
                onMoreClick = onMoreClick
            )
        }
    }
}

@FeedContentPreviews
@Composable
private fun FeedContentPreview(
    @PreviewParameter(FeedContentPreviewParameterProvider::class) feedItem: FeedItem
) {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedContent(
                feedItem = feedItem,
                onProfileClick = {},
                onMoreClick = {}
            )
        }
    }
}

class FeedContentPreviewParameterProvider : PreviewParameterProvider<FeedItem> {

    override val values: Sequence<FeedItem>
        get() = sequenceOf(
            FeedItem(
                feedId = "1",
                userId = "user1",
                userName = "Tester",
                userProfileImage = "",
                caption = "Test Caption, Test Caption, Test Caption, Test Caption, Test Caption, Test Caption, Test Caption, Test Caption, Test Caption, ",
                likeCount = 10,
                commentCount = 5,
                isLiked = true,
                type = FeedItemType.Mine(
                    imageUrls = persistentListOf("https://picsum.photos/400/400"),
                    timestamp = Timestamp.HoursAgo(12)
                )
            ),
            FeedItem(
                feedId = "1",
                userId = "user1",
                userName = "Android",
                userProfileImage = "",
                caption = "Test Caption 1",
                likeCount = 10,
                commentCount = 5,
                isLiked = false,
                type = FeedItemType.Sponsored(
                    imageUrl = "https://picsum.photos/400/400",
                    adUrl = "https://www.naver.com"
                )
            )
        )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class FeedContentPreviews