package com.dhkim.feed.common

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.LoadingSpinner
import com.dhkim.ui.noRippleClick
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.flowOf

@Composable
fun FeedCommentBottomSheet(
    userProfileImageUrl: String,
    comments: LazyPagingItems<CommentItem>,
    addComment: (String) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
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
                                    style = InstagramTheme.typography.bodySmallGray
                                )

                                if (comment.replyCount > 0) {
                                    Text(
                                        text = context.getString(R.string.view_replies, comment.replyCount),
                                        style = InstagramTheme.typography.bodySmallGray,
                                        modifier = Modifier
                                            .padding(start = 18.dp)
                                    )
                                }
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
                                    text = "${comment.replyCount}",
                                    style = InstagramTheme.typography.bodySmallGray
                                )
                            }
                        }
                    }
                }
            }
        }
        CommentTextFiled(
            userProfileImageUrl = userProfileImageUrl,
            addComment = addComment,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
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
                .height(36.dp),
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
                userProfileImageUrl = "userProfileImageUrl",
                comments = comments,
                addComment = {}
            )
        }
    }
}

@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class FeedCommentBottomSheetPreviews