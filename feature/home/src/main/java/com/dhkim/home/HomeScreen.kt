package com.dhkim.home

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.UploadState
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeScreen(
    feedUploadStatuses: ImmutableList<FeedUploadStatus>,
    feeds: LazyPagingItems<Feed>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        items(
            count = feeds.itemCount,
            key = feeds.itemKey(),
            contentType = feeds.itemContentType()
        ) { index ->
            if (index >= 0 && index < feedUploadStatuses.size) {
                FeedUploadStatusContent(feedUploadStatus = feedUploadStatuses[index])
            } else {
                feeds[index - feedUploadStatuses.size]?.run {
                    Text(
                        text = "$feedId, $userId, $userName, $userProfileImage, $imageUrls, $caption, $timestamp, $likeCount, $commentCount",
                        style = InstagramTheme.typography.bodyLargeBold,
                        modifier = Modifier
                            .testTag("feed_$index")
                    )
                }
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
                )
            )
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
                feedUploadStatuses = feedUploadStatuses,
                feeds = mockFeeds
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class HomeScreenPreviews