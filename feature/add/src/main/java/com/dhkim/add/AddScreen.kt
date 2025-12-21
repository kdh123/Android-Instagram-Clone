package com.dhkim.add

import android.Manifest
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.feed.model.Feed
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun AddScreen(
    onAction: (AddAction) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        imageUri = uri
    }
    val imagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { state ->
        val isGranted = state.keys.count { state[it] == false } == 0
        if (isGranted) {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        GlideImage(
            imageModel = { imageUri ?: "" },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f),
            previewPlaceholder = painterResource(R.drawable.ic_dummy_background)
        )

        Text(
            text = "AddScreen",
            style = InstagramTheme.typography.bodyLargeBold
        )
        Button(
            onClick = {
                imagePermissionLauncher.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                )
            }
        ) {
            Text(
                text = "Upload Image"
            )
        }

        Button(
            onClick = {
                onAction(
                    AddAction.UploadFeed(
                        feed = Feed(
                            caption = "Hello World"
                        ),
                        imageUrls = listOf("$imageUri")
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