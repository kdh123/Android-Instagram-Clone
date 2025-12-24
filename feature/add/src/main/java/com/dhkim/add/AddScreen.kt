package com.dhkim.add

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.dhkim.domain.common.model.GalleryImage
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    galleryImages: LazyPagingItems<GalleryImage>,
    selectImageMode: SelectImageMode,
    onAction: (AddAction) -> Unit,
    onBack: () -> Unit
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                tint = InstagramTheme.colors.onBackground,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clickable(onClick = onBack)
            )
            Text(
                text = stringResource(R.string.new_feed),
                style = InstagramTheme.typography.titleMedium,
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f)
            )
            Text(
                text = stringResource(R.string.next),
                style = InstagramTheme.typography.labelMediumBold,
                color = InstagramTheme.colors.primary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyVerticalGrid(
                state = addState.galleryListState,
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(
                    count = galleryImages.itemCount,
                    key = galleryImages.itemKey(),
                    contentType = galleryImages.itemContentType(),
                    span = { index ->
                        val span = if (index == 0 || index == 1) maxLineSpan else 1
                        GridItemSpan(span)
                    }
                ) { index ->
                    when (index) {
                        0 -> {
                            val selectedGalleryImage = when (selectImageMode) {
                                is SelectImageMode.Single -> selectImageMode.imageUri
                                is SelectImageMode.Multiple -> if (selectImageMode.imageUris.isNotEmpty()) {
                                    selectImageMode.imageUris[0]
                                } else {
                                    null
                                }
                            }

                            GlideImage(
                                imageModel = { selectedGalleryImage ?: "" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.0f),
                                previewPlaceholder = painterResource(R.drawable.ic_dummy_background)
                            )
                        }

                        1 -> {
                            SelectMultipleImagesButton(
                                onClick = {}
                            )
                        }

                        else -> {
                            val galleryImage = galleryImages[index - 2]
                            if (galleryImage != null) {
                                val isSelected = when (selectImageMode) {
                                    is SelectImageMode.Single -> selectImageMode.imageUri == galleryImage.uri
                                    is SelectImageMode.Multiple -> selectImageMode.imageUris.contains(galleryImage.uri)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.0f),
                                ) {
                                    GlideImage(
                                        imageModel = { galleryImage.uri },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.0f),
                                        previewPlaceholder = painterResource(R.drawable.ic_dummy_background)
                                    )


                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1.0f)
                                                .background(color = Color.Black.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (addState.isAtTop) {
                SelectMultipleImagesButton(
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun SelectMultipleImagesButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = InstagramTheme.colors.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .clip(shape = RoundedCornerShape(24.dp))
                .background(color = Color.DarkGray)
                .padding(8.dp)
                .align(Alignment.CenterEnd)
                .clickable(onClick = onClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_multiple_images),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(16.dp)
            )
            Text(
                text = stringResource(R.string.select_multiple_images),
                style = InstagramTheme.typography.labelSmall,
                color = Color.White,
            )
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
                selectImageMode = SelectImageMode.Single(),
                onAction = {},
                onBack = {}
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class AddScreenPreviews