package com.dhkim.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.feed.model.Feed
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeScreen(
    feeds: LazyPagingItems<Feed>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = feeds.itemCount,
            key = feeds.itemKey(),
            contentType = feeds.itemContentType()
        ) { index ->
            val feed = feeds[index]
            feed?.run {
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

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                feeds = mockFeeds
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class HomeScreenPreviews