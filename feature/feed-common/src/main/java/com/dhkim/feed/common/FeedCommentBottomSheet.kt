package com.dhkim.feed.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.designsystem.InstagramTheme
import kotlinx.coroutines.flow.flowOf

@Composable
fun FeedCommentBottomSheet() {
}

@FeedCommentBottomSheetPreviews
@Composable
private fun FeedCommentBottomSheetPreview() {
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
    val items = mockUsersFlow.collectAsLazyPagingItems()

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedCommentBottomSheet()
        }
    }
}

@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class FeedCommentBottomSheetPreviews