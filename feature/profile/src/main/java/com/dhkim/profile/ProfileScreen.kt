package com.dhkim.profile

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
fun ProfileScreen() {
    Text(
        text = "ProfileScreen",
        style = InstagramTheme.typography.bodyLargeBold
    )
}

@ProfileScreenPreviews
@Composable
private fun ProfileScreenPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileScreen()
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class ProfileScreenPreviews