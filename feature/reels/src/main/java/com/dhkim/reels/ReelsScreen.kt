package com.dhkim.reels

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dhkim.designsystem.InstagramTheme

@Composable
fun ReelsScreen() {
    Text(
        text = "ReelsScreen",
        style = InstagramTheme.typography.bodyLargeBold
    )
}

@ReelsScreenPreviews
@Composable
private fun ReelsScreenPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ReelsScreen()
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class ReelsScreenPreviews