@file:JvmName("FeedHeaderTypesKt")

package com.dhkim.feed.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.noRippleClick
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.persistentListOf

@Composable
private fun FeedHeader(
    feed: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit,
    content: @Composable FeedHeaderScope.() -> Unit,
) {
    val scope = remember(feed) { DefaultFeedHeaderScope(feed) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            GlideImage(
                imageModel = { feed.userProfileImage },
                previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
                modifier = Modifier
                    .padding(6.dp)
                    .clip(CircleShape)
                    .size(36.dp)
                    .noRippleClick(onClick = onProfileClick)
            )

            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(0.dp)
                    .weight(1f)
            ) {
                scope.content()
            }

            Icon(
                imageVector = Icons.Outlined.MoreVert,
                tint = InstagramTheme.colors.onBackground,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(24.dp)
                    .noRippleClick { onMoreClick(feed) }
            )
        }
    }
}

@Composable
fun MyFeedHeader(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    FeedHeader(
        feed = feedItem,
        onProfileClick = onProfileClick,
        onMoreClick = onMoreClick
    ) {
        Text(
            text = feed.userName,
            style = InstagramTheme.typography.labelMediumBold,
            modifier = Modifier
        )
    }
}

@FeedHeaderPreviews
@Composable
private fun MyHeaderPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            MyFeedHeader(
                feedItem = FeedItem(
                    feedId = "1",
                    userId = "user1",
                    userName = "Tester",
                    userProfileImage = "",
                    caption = "Test Caption 1",
                    likeCount = 10,
                    representativeLikeId = "user1",
                    representativeLikeName = "Tom",
                    commentCount = 5,
                    isLiked = false,
                    isLikeCountVisible = true,
                    isCommentEnabled = true,
                    type = FeedItemType.Mine(
                        imageUrls = persistentListOf("https://picsum.photos/400/400"),
                        timestamp = System.currentTimeMillis().toRelativeTime()
                    )
                ),
                onProfileClick = {},
                onMoreClick = {}
            )
        }
    }
}

@Composable
fun FollowingFeedHeader(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    FeedHeader(
        feed = feedItem,
        onProfileClick = onProfileClick,
        onMoreClick = onMoreClick
    ) {
        Text(
            text = feed.userName,
            style = InstagramTheme.typography.labelMediumBold
        )
    }
}

@FeedHeaderPreviews
@Composable
private fun FollowingHeaderPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            FollowingFeedHeader(
                feedItem = FeedItem(
                    feedId = "1",
                    userId = "user1",
                    userName = "Following",
                    userProfileImage = "",
                    caption = "Test Caption 1",
                    likeCount = 10,
                    representativeLikeId = "user1",
                    representativeLikeName = "Tom",
                    commentCount = 5,
                    isLiked = false,
                    isLikeCountVisible = true,
                    isCommentEnabled = true,
                    type = FeedItemType.Following(
                        imageUrls = persistentListOf("https://picsum.photos/400/400"),
                        timestamp = System.currentTimeMillis().toRelativeTime()
                    )
                ),
                onProfileClick = {},
                onMoreClick = {}
            )
        }
    }
}

@Composable
fun SuggestedFeedHeader(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    FeedHeader(
        feed = feedItem,
        onProfileClick = onProfileClick,
        onMoreClick = onMoreClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f)
            ) {
                Text(
                    text = feed.userName,
                    style = InstagramTheme.typography.labelMediumBold
                )
                Text(
                    text = stringResource(R.string.feed_suggested_label),
                    style = InstagramTheme.typography.labelSmall
                )
            }

            Text(
                text = stringResource(R.string.follow),
                style = InstagramTheme.typography.labelMediumBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(color = InstagramTheme.colors.secondary)
                    .padding(10.dp)

            )
        }
    }
}

@FeedHeaderPreviews
@Composable
private fun SuggestedHeaderPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            SuggestedFeedHeader(
                feedItem = FeedItem(
                    feedId = "1",
                    userId = "user1",
                    userName = "Suggested",
                    userProfileImage = "",
                    caption = "Test Caption 1",
                    likeCount = 10,
                    representativeLikeId = "user1",
                    representativeLikeName = "Tom",
                    commentCount = 5,
                    isLiked = false,
                    isLikeCountVisible = true,
                    isCommentEnabled = true,
                    type = FeedItemType.Suggested(
                        imageUrls = persistentListOf("https://picsum.photos/400/400"),
                        timestamp = System.currentTimeMillis().toRelativeTime()
                    )
                ),
                onProfileClick = {},
                onMoreClick = {}
            )
        }
    }
}

@Composable
fun SponsoredFeedHeader(
    feedItem: FeedItem,
    onProfileClick: () -> Unit,
    onMoreClick: (FeedItem) -> Unit
) {
    FeedHeader(
        feed = feedItem,
        onProfileClick = onProfileClick,
        onMoreClick = onMoreClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f)
            ) {
                Text(
                    text = feed.userName,
                    style = InstagramTheme.typography.labelMediumBold
                )
                Text(
                    text = stringResource(R.string.sponsored),
                    style = InstagramTheme.typography.labelSmall
                )
            }
        }
    }
}

@FeedHeaderPreviews
@Composable
private fun SponsoredHeaderPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            SponsoredFeedHeader(
                feedItem = FeedItem(
                    feedId = "1",
                    userId = "user1",
                    userName = "Sponsored",
                    userProfileImage = "",
                    caption = "Test Caption 1",
                    likeCount = 10,
                    representativeLikeId = "user1",
                    representativeLikeName = "Tom",
                    commentCount = 5,
                    isLiked = false,
                    isLikeCountVisible = true,
                    isCommentEnabled = true,
                    type = FeedItemType.Sponsored(
                        imageUrl = "https://picsum.photos/400/400",
                        adUrl = "https://www.naver.com"
                    )
                ),
                onProfileClick = {},
                onMoreClick = {}
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class FeedHeaderPreviews