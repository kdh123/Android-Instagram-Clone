package com.dhkim.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import com.dhkim.designsystem.InstagramTheme

@Composable
fun HomeScreen() {
    Text(
        text = "HomeScreen",
        style = InstagramTheme.typography.bodyLargeBold
    )
}

@HomeScreenPreviews
@Composable
private fun HomeScreenPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen()
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class HomeScreenPreviews