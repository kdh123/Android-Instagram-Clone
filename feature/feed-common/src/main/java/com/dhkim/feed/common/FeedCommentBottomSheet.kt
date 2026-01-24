package com.dhkim.feed.common

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.LoadingSpinner
import com.dhkim.ui.advancedImePadding
import com.dhkim.ui.noRippleClick
import com.dhkim.ui.pxToDp
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf

@JvmInline
value class Comment(val value: String)

@Composable
fun FeedCommentBottomSheet(
    lazyListState: LazyListState,
    userProfileImageUrl: String,
    comments: LazyPagingItems<CommentItem>,
    replies: ImmutableList<ReplyGroup>,
    addComment: (String) -> Unit,
    deleteComment: (CommentItem) -> Unit,
    addReply: (CommentItem, Comment) -> Unit,
    showReplies: (CommentItem) -> Unit,
) {
    val context = LocalContext.current
    var replyToComment: CommentItem? by rememberSaveable { mutableStateOf(null) }
    var isDragStartedAtTop by rememberSaveable { mutableStateOf(false) }
    var commentTextFiledHeight by remember { mutableStateOf(0) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // 1. Remove onPreScroll (Important!): Ensure the list receives scroll events first.
            // 2. Implement onPostScroll: Handle "remaining delta" after the list finishes scrolling.
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // If there's leftover scroll delta (available.y > 0 -> dragging down) because the list cannot scroll further,
                // but the touch gesture did not start at the top:
                // -> Consume the event here to prevent it from propagating to the parent (BottomSheet).
                if (available.y > 0 && !isDragStartedAtTop) {
                    return available
                }
                return super.onPostScroll(consumed, available, source)
            }

            // 3. onPostFling: Prevent the sheet from closing due to inertia/fling.
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 0) return available
                return super.onPostFling(consumed, available)
            }
        }
    }
    var focusedComment by remember { mutableStateOf<CommentItem?>(null) }
    var commentYOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = commentTextFiledHeight.pxToDp())
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isDragStartedAtTop = !lazyListState.canScrollBackward
                    }
                }
                .nestedScroll(nestedScrollConnection)
        ) {
            items(
                count = comments.itemCount + 2,
                key = { index ->
                    if (index < 2) {
                        "$index"
                    } else {
                        comments.itemKey { it.commentId }.invoke(index - 2)
                    }
                },
                contentType = { index ->
                    when (index) {
                        0 -> "title"
                        1 -> "loading"
                        else -> comments.itemContentType { "comment_item" }.invoke(index - 2)
                    }
                }
            ) { index ->
                when (index) {
                    0 -> {
                        Text(
                            text = stringResource(R.string.comment),
                            textAlign = TextAlign.Center,
                            style = InstagramTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                    1 -> {
                        when {
                            comments.loadState.refresh is LoadState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    LoadingSpinner(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                    )
                                }
                            }

                            comments.loadState.refresh is LoadState.NotLoading && comments.itemCount <= 0 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_comments_yet),
                                        style = InstagramTheme.typography.headlineLarge,
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )

                                    Text(
                                        text = stringResource(R.string.be_the_first_to_comment),
                                        style = InstagramTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        val comment = comments[index - 2] ?: return@items
                        val wholeReplies = replies.firstOrNull { it.commentId == comment.commentId }?.replies ?: persistentListOf()
                        val recentAddedReplies = replies.firstOrNull { it.commentId == comment.commentId }?.recentAddedReplies ?: persistentListOf()
                        val windowInsets = WindowInsets.navigationBars.asPaddingValues()
                        val statusBarHeight = with(LocalDensity.current) { windowInsets.calculateBottomPadding().toPx() }
                        val bottomNavigationBarHeight = with(LocalDensity.current) { 80.dp.toPx() }

                        CommentRow(
                            comment = comment,
                            wholeReplies = wholeReplies,
                            recentAddedReplies = recentAddedReplies,
                            onReplyClick = { replyToComment = comment },
                            showReplies = { showReplies(comment) },
                            onLongClick = { focusedComment = comment },
                            modifier = Modifier
                                .onGloballyPositioned {
                                    if (focusedComment != null && focusedComment!!.commentId == comment.commentId) {
                                        commentYOffset = it.positionInWindow().y - statusBarHeight - bottomNavigationBarHeight
                                    }
                                }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .advancedImePadding()
                .onGloballyPositioned {
                    commentTextFiledHeight = it.size.height
                }
        ) {
            if (replyToComment != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = InstagramTheme.colors.secondary)
                        .padding(8.dp)
                        .testTag("reply_to_username")
                ) {
                    Text(
                        text = context.getString(R.string.reply_posting, replyToComment!!.userName),
                        style = InstagramTheme.typography.bodyMedium,
                        color = InstagramTheme.colors.onSecondary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .width(0.dp)
                            .weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = InstagramTheme.colors.onSecondary,
                        modifier = Modifier
                            .noRippleClick {
                                replyToComment = null
                            }
                    )
                }
            }

            CommentTextFiled(
                userProfileImageUrl = userProfileImageUrl,
                addComment = { content ->
                    if (replyToComment == null) {
                        addComment(content)
                    } else {
                        addReply(replyToComment!!, Comment(content))
                        replyToComment = null
                    }
                }
            )
        }

        if (focusedComment != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.7f))
                    .noRippleClick { focusedComment = null }
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = with(LocalDensity.current) { commentYOffset.toDp() })
                ) {
                    FocusedCommentRow(
                        comment = focusedComment!!,
                        deleteComment = {
                            deleteComment(focusedComment!!)
                            focusedComment = null
                        },
                        onDismiss = { focusedComment = null }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentRow(
    comment: CommentItem,
    wholeReplies: ImmutableList<ReplyItem>,
    recentAddedReplies: ImmutableList<ReplyItem>,
    onReplyClick: () -> Unit,
    showReplies: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var shouldShowReplies by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .testTag("comment_item_${comment.commentId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            GlideImage(
                imageModel = { comment.userProfileImageUrl },
                previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(42.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = comment.userName,
                        style = InstagramTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(end = 4.dp)
                    )

                    val timeAt = when (comment.timeAt) {
                        is Timestamp.JustNow -> stringResource(R.string.time_just_now)
                        is Timestamp.MinutesAgo -> context.getString(R.string.time_minutes_ago, comment.timeAt.minutes)
                        is Timestamp.HoursAgo -> context.getString(R.string.time_hours_ago, comment.timeAt.hours)
                        is Timestamp.DaysAgo -> context.getString(R.string.time_days_ago, comment.timeAt.days)
                        is Timestamp.Date -> comment.timeAt.date
                    }

                    Text(
                        text = timeAt,
                        style = InstagramTheme.typography.bodySmallGray,
                    )
                }

                Text(
                    text = comment.content,
                    style = InstagramTheme.typography.bodyMedium
                )

                Text(
                    text = stringResource(R.string.reply),
                    style = InstagramTheme.typography.bodySmallGray,
                    modifier = Modifier
                        .noRippleClick(onClick = onReplyClick)
                        .testTag("reply_button_${comment.commentId}")
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .noRippleClick { }
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .size(16.dp)
                )
                Text(
                    text = "${comment.likeCount}",
                    style = InstagramTheme.typography.bodySmallGray
                )
            }
        }

        if (recentAddedReplies.isNotEmpty() && !shouldShowReplies) {
            Replies(replies = recentAddedReplies)
        }

        if (comment.replyCount > 0) {
            if (shouldShowReplies) {
                Replies(replies = wholeReplies)
            } else {
                Text(
                    text = context.getString(R.string.view_replies, comment.replyCount),
                    style = InstagramTheme.typography.bodySmallGray,
                    modifier = Modifier
                        .padding(start = 60.dp)
                        .noRippleClick {
                            shouldShowReplies = true
                            showReplies()
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusedCommentRow(
    comment: CommentItem,
    deleteComment: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = InstagramTheme.colors.secondary)
                .padding(vertical = 10.dp)
                .noRippleClick(onClick = onDismiss)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                GlideImage(
                    imageModel = { comment.userProfileImageUrl },
                    previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(42.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .width(0.dp)
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = comment.userName,
                            style = InstagramTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(end = 4.dp)
                        )

                        val timeAt = when (comment.timeAt) {
                            is Timestamp.JustNow -> stringResource(R.string.time_just_now)
                            is Timestamp.MinutesAgo -> context.getString(R.string.time_minutes_ago, comment.timeAt.minutes)
                            is Timestamp.HoursAgo -> context.getString(R.string.time_hours_ago, comment.timeAt.hours)
                            is Timestamp.DaysAgo -> context.getString(R.string.time_days_ago, comment.timeAt.days)
                            is Timestamp.Date -> comment.timeAt.date
                        }

                        Text(
                            text = timeAt,
                            style = InstagramTheme.typography.bodySmallGray,
                        )
                    }

                    Text(
                        text = comment.content,
                        style = InstagramTheme.typography.bodyMedium
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .noRippleClick { }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Color.Gray,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(16.dp)
                    )
                    Text(
                        text = "${comment.likeCount}",
                        style = InstagramTheme.typography.bodySmallGray
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .background(color = InstagramTheme.colors.secondary)
                .padding(10.dp)
                .noRippleClick(onClick = deleteComment)
                .testTag("delete_comment_button_${comment.commentId}")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    tint = InstagramTheme.colors.error,
                    contentDescription = "deleteComment",
                    modifier = Modifier
                        .padding(end = 4.dp)
                )
                Text(
                    text = stringResource(R.string.delete),
                    style = InstagramTheme.typography.bodyMedium,
                    color = InstagramTheme.colors.error
                )
            }
        }
    }
}

@Composable
fun Replies(
    replies: ImmutableList<ReplyItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 60.dp)
    ) {
        replies.forEach { item ->
            ReplyRow(reply = item)
        }
    }
}

@Composable
fun ReplyRow(
    reply: ReplyItem,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("reply_item_${reply.replyId}")
    ) {
        GlideImage(
            imageModel = { reply.user.profileImageUrl },
            previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
            modifier = Modifier
                .clip(CircleShape)
                .size(42.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .width(0.dp)
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = reply.user.name,
                    style = InstagramTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(end = 4.dp)
                )

                val timeAt = when (reply.timeAt) {
                    is Timestamp.JustNow -> stringResource(R.string.time_just_now)
                    is Timestamp.MinutesAgo -> context.getString(R.string.time_minutes_ago, reply.timeAt.minutes)
                    is Timestamp.HoursAgo -> context.getString(R.string.time_hours_ago, reply.timeAt.hours)
                    is Timestamp.DaysAgo -> context.getString(R.string.time_days_ago, reply.timeAt.days)
                    is Timestamp.Date -> reply.timeAt.date
                }

                Text(
                    text = timeAt,
                    style = InstagramTheme.typography.bodySmallGray,
                )
            }

            Text(
                text = reply.content,
                style = InstagramTheme.typography.bodyMedium
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(end = 10.dp)
                .noRippleClick { }
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(16.dp)
            )
            Text(
                text = "${reply.likeCount}",
                style = InstagramTheme.typography.bodySmallGray
            )
        }
    }
}

@Composable
private fun CommentTextFiled(
    userProfileImageUrl: String,
    addComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var comment by rememberSaveable { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = InstagramTheme.colors.background)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        GlideImage(
            imageModel = { userProfileImageUrl },
            previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
            modifier = Modifier
                .clip(CircleShape)
                .size(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = comment,
            onValueChange = { comment = it },
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .testTag("comment_text_field"),
            textStyle = InstagramTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Start,
                color = InstagramTheme.colors.onBackground,
            ),
            singleLine = true,
            cursorBrush = SolidColor(InstagramTheme.colors.onSecondary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = InstagramTheme.colors.onSecondary,
                            shape = RoundedCornerShape(18.dp),
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (comment.isEmpty()) {
                            Text(
                                text = stringResource(R.string.add_comment_placeholder),
                                style = InstagramTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }

                    AnimatedVisibility(
                        visible = comment.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(color = InstagramTheme.colors.primary)
                                .noRippleClick(onClick = {
                                    addComment(comment)
                                    comment = ""
                                })
                                .testTag("add_comment_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}

@FeedCommentBottomSheetPreviews
@Composable
private fun FeedCommentBottomSheetPreview() {
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

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedCommentBottomSheet(
                lazyListState = rememberLazyListState(),
                userProfileImageUrl = "userProfileImageUrl",
                comments = comments,
                replies = persistentListOf(),
                addComment = {},
                deleteComment = {},
                addReply = { _, _ -> },
                showReplies = {}
            )
        }
    }
}

@FeedCommentBottomSheetPreviews
@Composable
private fun FocusedCommentPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            FocusedCommentRow(
                comment = CommentItem(
                    commentId = "id",
                    userId = "userId",
                    userName = "Tester",
                    userProfileImageUrl = "",
                    content = "Test Comment",
                    timeAt = Timestamp.JustNow,
                    replyCount = 3,
                    likeCount = 10
                ),
                deleteComment = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class FeedCommentBottomSheetPreviews