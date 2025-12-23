package com.dhkim.add

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.common.model.GalleryImage
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.flowOf

@Composable
fun AddScreen(
    galleryImages: LazyPagingItems<GalleryImage>,
    onAction: (AddAction) -> Unit
) {
    val addState = rememberAddState()
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
        LazyVerticalGrid(
            state = addState.galleryListState,
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(start = 2.dp, end = 2.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(
                count = galleryImages.itemCount,
                key = galleryImages.itemKey(),
                contentType = galleryImages.itemContentType(),
                span = { index ->
                    val span = if (index == 0) maxLineSpan else 1
                    GridItemSpan(span)
                }
            ) { index ->
                val galleryImage = galleryImages[index]
                galleryImage?.run {
                    GlideImage(
                        imageModel = { uri },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.0f),
                        previewPlaceholder = painterResource(R.drawable.ic_dummy_background)
                    )
                }
            }
        }
    }
}

@AddScreenPreviews
@Composable
private fun AddScreenPreview() {
    val mockGalleyImages = flowOf(
        PagingData.from(
            mutableListOf<GalleryImage>().apply {
                repeat(10) {
                    add(
                        GalleryImage(
                            id = it.toLong(),
                            uri = "imageUri",
                            name = "",
                            dateAdded = 0L
                        )
                    )
                }
            }
        )
    )

    val galleryImages = mockGalleyImages.collectAsLazyPagingItems()

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AddScreen(
                galleryImages = galleryImages,
                onAction = {}
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class AddScreenPreviews