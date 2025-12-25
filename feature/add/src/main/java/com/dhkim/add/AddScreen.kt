package com.dhkim.add

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
    selectImageState: SelectImageState,
    onAction: (AddAction) -> Unit,
    onBack: () -> Unit
) {
    val addState = rememberAddState()
    val imagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { state ->
        val isGranted = state.keys.count { state[it] == false } == 0
        if (isGranted) {

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
                            val selectedGalleryImage: String? = when (selectImageState) {
                                is SelectImageState.Single -> selectImageState.imageUri
                                is SelectImageState.Multiple -> selectImageState.currentImage
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
                                selectImageState = selectImageState,
                                onClick = { onAction(AddAction.ChangeSelectImageMode) }
                            )
                        }

                        else -> {
                            val galleryImage = galleryImages[index - 2] ?: return@items
                            val isSelected = when (selectImageState) {
                                is SelectImageState.Single -> selectImageState.imageUri == galleryImage.uri
                                is SelectImageState.Multiple -> selectImageState.selectedImages
                                    .map { it.imageUri }
                                    .contains(galleryImage.uri)
                            }

                            if (isSelected) {
                                when (selectImageState) {
                                    is SelectImageState.Single -> {
                                        SelectedImage(
                                            imageUri = galleryImage.uri,
                                            onAction = onAction
                                        )
                                    }

                                    is SelectImageState.Multiple -> {
                                        val number = selectImageState.selectedImages
                                            .firstOrNull { it.imageUri == galleryImage.uri }
                                            ?.number
                                            ?: 1
                                        SelectedImageInMultipleSelectImageMode(
                                            number = number,
                                            imageUri = galleryImage.uri,
                                            onAction = onAction
                                        )
                                    }
                                }
                            } else {
                                NotSelectedImage(
                                    imageUri = galleryImage.uri,
                                    onAction = onAction
                                )
                            }
                        }
                    }
                }
            }

            if (addState.isAtTop) {
                SelectMultipleImagesButton(
                    selectImageState = selectImageState,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun NotSelectedImage(
    imageUri: String,
    onAction: (AddAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .clickable {
                onAction(
                    AddAction.SelectImage(imageUri = imageUri)
                )
            },
    ) {
        GlideImage(
            imageModel = { imageUri },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f),
            previewPlaceholder = painterResource(R.drawable.ic_dummy_background)
        )
    }
}

@Composable
fun SelectedImage(
    imageUri: String,
    onAction: (AddAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .clickable {
                onAction(
                    AddAction.SelectImage(imageUri = imageUri)
                )
            },
    ) {
        GlideImage(
            imageModel = { imageUri },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f),
            previewPlaceholder = painterResource(R.drawable.ic_dummy_background)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f)
                .background(color = Color.White.copy(alpha = 0.7f))
        )
    }
}

@Composable
fun SelectedImageInMultipleSelectImageMode(
    number: Int,
    imageUri: String,
    onAction: (AddAction) -> Unit
) {
    Box {
        SelectedImage(
            imageUri = imageUri,
            onAction = onAction
        )

        Box(
            modifier = Modifier
                .padding(6.dp)
                .clip(CircleShape)
                .size(24.dp)
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = CircleShape
                )
                .background(color = InstagramTheme.colors.primary)
                .align(Alignment.TopEnd)
        ) {
            Text(
                text = "$number",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = InstagramTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.Center)

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectImageInMultipleSelectImageModePreview() {
    SelectedImageInMultipleSelectImageMode(
        number = 1,
        imageUri = "",
        onAction = {}
    )
}

@Composable
fun SelectMultipleImagesButton(
    selectImageState: SelectImageState,
    onClick: () -> Unit
) {
    val buttonText = when (selectImageState) {
        is SelectImageState.Single -> stringResource(R.string.select_multiple_images)
        is SelectImageState.Multiple -> stringResource(R.string.cancel)
    }

    val buttonBackgroundColor = when (selectImageState) {
        is SelectImageState.Single -> Color.DarkGray
        is SelectImageState.Multiple -> Color.White
    }

    val buttonContentColor = when (selectImageState) {
        is SelectImageState.Single -> Color.White
        is SelectImageState.Multiple -> Color.Black
    }

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
                .background(color = buttonBackgroundColor)
                .padding(8.dp)
                .align(Alignment.CenterEnd)
                .clickable(onClick = onClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_multiple_images),
                contentDescription = null,
                tint = buttonContentColor,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(16.dp)
            )
            Text(
                text = buttonText,
                style = InstagramTheme.typography.labelSmall,
                color = buttonContentColor,
            )
        }
    }
}

@AddScreenPreviews
@Composable
private fun AddScreenPreview(
    @PreviewParameter(AddScreenPreviewParameterProvider::class) selectImageState: SelectImageState
) {
    val mockGalleyImages = flowOf(
        PagingData.from(
            mutableListOf<GalleryImage>().apply {
                repeat(10) {
                    add(
                        GalleryImage(
                            id = it.toLong(),
                            uri = "imageUri$it",
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
                selectImageState = selectImageState,
                onAction = {},
                onBack = {}
            )
        }
    }
}

class AddScreenPreviewParameterProvider : PreviewParameterProvider<SelectImageState> {

    override val values: Sequence<SelectImageState>
        get() = sequenceOf(
            SelectImageState.Single("imageUri0"),
            SelectImageState.Multiple(
                currentImage = "imageUri0",
                selectedImages = listOf("imageUri0", "imageUri2", "imageUri3")
                    .map {
                        SelectedImage(number = 1, imageUri = it)
                    }
            )
        )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class AddScreenPreviews