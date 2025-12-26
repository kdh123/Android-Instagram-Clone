package com.dhkim.add

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.bumptech.glide.request.RequestOptions
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.common.model.GalleryImage
import com.dhkim.ui.detectTransformGesturesWithEnd
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max

@SuppressLint("UnusedBoxWithConstraintsScope")
@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    addState: AddState,
    galleryImages: LazyPagingItems<GalleryImage>,
    selectImageState: SelectImageState,
    onAction: (AddAction) -> Unit,
    onBack: () -> Unit
) {
    val imagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { state ->
        val isGranted = state.keys.count { state[it] == false } == 0
        if (isGranted) {

        }
    }

    BoxWithConstraints {
        val screenWidth = maxWidth
        val topBarHeight = 64.dp
        val buttonHeight = 64.dp
        val screenHeight = maxHeight - topBarHeight - buttonHeight
        val itemHeight = screenWidth / 4 + WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()
        val gridBottomPadding = screenHeight - itemHeight

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TopBar(onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(state = addState.galleryScrollState)
            ) {
                PreviewSelectedImage(
                    selectImageState = selectImageState,
                    onAction = onAction
                )
                SelectMultipleImagesButton(
                    selectImageState = selectImageState,
                    onClick = { onAction(AddAction.ChangeSelectImageMode) }
                )
                LazyVerticalGrid(
                    state = addState.galleryListState,
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(
                        bottom = gridBottomPadding + WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = InstagramTheme.colors.background)
                        .height(screenHeight)
                        .nestedScroll(remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    return if (available.y > 0) Offset.Zero else Offset(
                                        x = 0f,
                                        y = -addState.galleryScrollState.dispatchRawDelta(-available.y)
                                    )
                                }
                            }
                        })
                ) {
                    items(
                        count = galleryImages.itemCount,
                        key = galleryImages.itemKey(),
                        contentType = galleryImages.itemContentType(),
                    ) { index ->
                        val galleryImage = galleryImages[index] ?: return@items
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
    }
}

@Composable
internal fun PreviewSelectedImage(
    selectImageState: SelectImageState,
    onAction: (AddAction) -> Unit
) {
    val selectedImageUri: String? = when (selectImageState) {
        is SelectImageState.Single -> selectImageState.imageUri
        is SelectImageState.Multiple -> selectImageState.currentImage?.imageUri
    }
    var intrinsicSize by remember { mutableStateOf(Size.Zero) }
    val aspectRatio = if (intrinsicSize == Size.Zero) 1f else intrinsicSize.width / intrinsicSize.height
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.Black)
            .onSizeChanged { viewportSize = it },
        contentAlignment = Alignment.Center
    ) {
        // Cropping area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .background(InstagramTheme.colors.background)
                .pointerInput(Unit) {
                    detectTransformGesturesWithEnd(
                        onGesture = { centroid, pan, zoom, _ ->
                            scope.launch {
                                // Handle Zoom
                                val nextScale = scale.value * zoom
                                scale.snapTo(nextScale)

                                // Handle Pan + Resistance
                                // Calculate how much larger the image is compared to the viewport based on current scale
                                val imageWidth = if (intrinsicSize.width / intrinsicSize.height <= 1f) {
                                    intrinsicSize.width * scale.value
                                } else {
                                    viewportSize.width * scale.value
                                }
                                val imageHeight = if (intrinsicSize.height / intrinsicSize.height <= 1f) {
                                    intrinsicSize.height * scale.value
                                } else {
                                    viewportSize.height * scale.value
                                }

                                // Maximum allowable offset (boundaries)
                                val maxOffsetX = (imageWidth - viewportSize.width) / 2f
                                val maxOffsetY = (imageHeight - viewportSize.height) / 2f

                                // Calculate resistance for X and Y axes
                                val resistanceX = if (offset.value.x.absoluteValue > maxOffsetX) 0.3f else 1f
                                val resistanceY = if (offset.value.y.absoluteValue > maxOffsetY) 0.3f else 1f

                                // Apply resistance to pan movement
                                val targetOffset = offset.value + Offset(
                                    pan.x * resistanceX,
                                    pan.y * resistanceY
                                )
                                offset.snapTo(targetOffset)
                            }
                        },
                        onGestureEnd = {
                            scope.launch {
                                // Bounce back if the image is out of boundaries when the gesture ends
                                // Limit minimum scale (Bounce back to 1.0 if smaller)
                                val targetScale = max(1f, scale.value)
                                launch {
                                    if (scale.value < 1f) {
                                        scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                                    }
                                }

                                // Recalculate boundaries based on target scale
                                val imageWidth = if (intrinsicSize.width / intrinsicSize.height <= 1f) {
                                    intrinsicSize.width * targetScale
                                } else {
                                    viewportSize.width * targetScale
                                }
                                val imageHeight = if (intrinsicSize.width / intrinsicSize.height <= 1f) {
                                    intrinsicSize.height * targetScale
                                } else {
                                    viewportSize.height * targetScale
                                }

                                val maxOffsetX = (imageWidth - viewportSize.width) / 2f
                                val maxOffsetY = (imageHeight - viewportSize.height) / 2f
                                val currentOffset = offset.value

                                // Clamp X and Y within boundaries
                                val newX = currentOffset.x.coerceIn(-maxOffsetX, maxOffsetX)
                                val newY = currentOffset.y.coerceIn(-maxOffsetY, maxOffsetY)

                                if (newX != currentOffset.x || newY != currentOffset.y) {
                                    offset.animateTo(
                                        Offset(newX, newY),
                                        spring(Spring.DampingRatioLowBouncy)
                                    )
                                }

                                onAction(AddAction.DragImage(scale.value, offset.value))
                            }
                        }
                    )
                }
        ) {
            GlideImage(
                imageModel = { selectedImageUri ?: "" },
                requestOptions = {
                    RequestOptions()
                        .override(viewportSize.width, viewportSize.height)
                },
                success = { _, painter ->
                    val initScale = when (selectImageState) {
                        is SelectImageState.Single -> 1f
                        is SelectImageState.Multiple -> selectImageState.currentImage?.scale ?: 1f
                    }
                    val initOffset = when (selectImageState) {
                        is SelectImageState.Single -> Offset.Zero
                        is SelectImageState.Multiple -> selectImageState.currentImage?.offset ?: Offset.Zero
                    }

                    scope.launch {
                        scale.snapTo(initScale)
                        offset.snapTo(initOffset)
                    }

                    intrinsicSize = painter.intrinsicSize
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offset.value.x
                        translationY = offset.value.y
                    },
                previewPlaceholder = painterResource(R.drawable.ic_dummy_background)
            )
        }
    }
}

@Composable
internal fun TopBar(
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 10.dp)
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
}

@Composable
internal fun NotSelectedImage(
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
internal fun SelectedImage(
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
internal fun SelectedImageInMultipleSelectImageMode(
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

@Composable
internal fun SelectMultipleImagesButton(
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
            .height(64.dp)
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
                addState = rememberAddState(),
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
            SelectImageState.Multiple(
                currentImage = SelectedImage(number = 1, imageUri = "imageUri0"),
                selectedImages = listOf("imageUri0", "imageUri2", "imageUri3")
                    .map {
                        SelectedImage(number = 1, imageUri = it)
                    }
            ),
            SelectImageState.Single("imageUri0"),
        )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
internal annotation class AddScreenPreviews