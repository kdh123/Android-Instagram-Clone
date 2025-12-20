package com.dhkim.add

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.feed.model.Feed

@Composable
fun AddScreen(
    onAction: (AddAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "AddScreen",
            style = InstagramTheme.typography.bodyLargeBold
        )
        Button(
            onClick = {
                onAction(
                    AddAction.UploadFeed(
                        feed = Feed(
                            feedId = "testFeedId",
                            userId = "testUserId",
                            userName = "testName",
                            userProfileImage = "testUserProfileImage",
                            imageUrls = listOf("imageUrl1", "imageUrl2"),
                            caption = "Hello World",
                            timestamp = 1234567890,
                            likeCount = 100,
                            commentCount = 50,
                        )
                    )
                )
            }
        ) {
            Text(
                text = "Upload Feed"
            )
        }
    }

}

@AddScreenPreviews
@Composable
private fun AddScreenPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AddScreen(
                onAction = {}
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class AddScreenPreviews