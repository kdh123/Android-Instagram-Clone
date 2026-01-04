package com.dhkim.feed.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.noRippleClick

@Composable
fun MyFeedBottomSheet(
    isLikeEnabled: Boolean,
    isCommentEnabled: Boolean,
    onLikeEnabledChange: (Boolean) -> Unit,
    onCommentEnabledChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = InstagramTheme.colors.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick {
                    onLikeEnabledChange(!isLikeEnabled)
                }
        ) {
            val likeIcon = if (isLikeEnabled) R.drawable.ic_cancel_like else R.drawable.ic_like
            val likeText = if (isLikeEnabled) R.string.hide_like_count else R.string.cancel_hide_like_count

            Icon(
                painter = painterResource(likeIcon),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(likeText),
                style = InstagramTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick {
                    onCommentEnabledChange(!isCommentEnabled)
                }
        ) {
            val commentIcon = if (isCommentEnabled) R.drawable.ic_disable_comment else R.drawable.ic_comment
            val commentText = if (isCommentEnabled) R.string.cancel_disable_comment else R.string.disable_comment

            Icon(
                painter = painterResource(commentIcon),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(commentText),
                style = InstagramTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onEditClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.edit),
                style = InstagramTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onDeleteClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                tint = InstagramTheme.colors.error,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.delete_media),
                style = InstagramTheme.typography.bodyMedium,
                color = InstagramTheme.colors.error
            )
        }
    }
}

@FeedBottomSheetPreviews
@Composable
private fun MyFeedBottomSheetPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            MyFeedBottomSheet(
                isLikeEnabled = true,
                isCommentEnabled = true,
                onLikeEnabledChange = {},
                onCommentEnabledChange = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
}

@Composable
fun FollowingFeedBottomSheet(
    isFollowing: Boolean,
    onFollowChanged: (Boolean) -> Unit,
    onNotInterestedClick: () -> Unit,
    onAccountInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = InstagramTheme.colors.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick {
                    onFollowChanged(!isFollowing)
                }
        ) {
            val followingIcon = if (isFollowing) R.drawable.ic_cancel_follow else R.drawable.ic_add_following
            val followingText = if (isFollowing) R.string.cancel_follow else R.string.follow

            Icon(
                painter = painterResource(followingIcon),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(followingText),
                style = InstagramTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onNotInterestedClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hide),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.hide_feed),
                style = InstagramTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onAccountInfoClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_account_info),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.account_info),
                style = InstagramTheme.typography.bodyMedium
            )
        }
    }
}

@FeedBottomSheetPreviews
@Composable
private fun FollowingFeedBottomSheetPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            FollowingFeedBottomSheet(
                isFollowing = true,
                onFollowChanged = {},
                onNotInterestedClick = {},
                onAccountInfoClick = {}
            )
        }
    }
}


@Composable
fun SuggestedFeedBottomSheet(
    onNotInterestedClick: () -> Unit,
    onAccountInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = InstagramTheme.colors.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onClick = onNotInterestedClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hide),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.hide_feed),
                style = InstagramTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onClick = onAccountInfoClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_account_info),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.account_info),
                style = InstagramTheme.typography.bodyMedium
            )
        }
    }
}

@FeedBottomSheetPreviews
@Composable
private fun SuggestedFeedBottomSheetPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            SuggestedFeedBottomSheet(
                onNotInterestedClick = {},
                onAccountInfoClick = {}
            )
        }
    }
}

@Composable
fun SponsoredFeedBottomSheet(
    onNotInterestedClick: () -> Unit,
    onAccountInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = InstagramTheme.colors.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onClick = onNotInterestedClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hide),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.hide_feed),
                style = InstagramTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .noRippleClick(onClick = onAccountInfoClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_account_info),
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
            )
            Text(
                text = stringResource(R.string.account_info),
                style = InstagramTheme.typography.bodyMedium
            )
        }
    }
}

@FeedBottomSheetPreviews
@Composable
private fun SponsoredFeedBottomSheetPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            SponsoredFeedBottomSheet(
                onNotInterestedClick = {},
                onAccountInfoClick = {}
            )
        }
    }
}

@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class FeedBottomSheetPreviews