package com.dhkim.feed.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dhkim.designsystem.InstagramTheme

@Composable
fun FeedImageLoadFailContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(color = Color.White)
    ) {
        Text(
            text = stringResource(R.string.feed_image_load_fail),
            style = InstagramTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Preview
@Composable
private fun FeedImageLoadFailContentPreview() {
    FeedImageLoadFailContent()
}