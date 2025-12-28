package com.dhkim.add

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.dhkim.designsystem.InstagramTheme
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlin.math.absoluteValue

@Composable
fun FeedUploadScreen(
    selectImageState: SelectImageState,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val currentSelectedImages = when (selectImageState) {
            is SelectImageState.Single -> listOf(selectImageState.currentImage)
            is SelectImageState.Multiple -> selectImageState.selectedImages
        }

        val pagerState = rememberPagerState(pageCount = { currentSelectedImages.size })

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer {
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                            val transformation = 1f - pageOffset.coerceIn(0f, 1f)

                            alpha = lerp(0.5f, 1f, transformation)
                            scaleX = lerp(0.85f, 1f, transformation)
                            scaleY = lerp(0.85f, 1f, transformation)
                            transformOrigin = TransformOrigin.Center
                        }
                ) {


                    GlideImage(
                        imageModel = { currentSelectedImages[page]?.imageUri ?: "" },
                        previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@FeedUploadScreenPreviews
@Composable
private fun FeedUploadScreenPreview(
    @PreviewParameter(FeedUploadScreenPreviewParameterProvider::class) selectImageState: SelectImageState
) {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedUploadScreen(
                selectImageState = selectImageState,
                onBack = {}
            )
        }
    }
}

class FeedUploadScreenPreviewParameterProvider : PreviewParameterProvider<SelectImageState> {

    override val values: Sequence<SelectImageState>
        get() = sequenceOf(
            SelectImageState.Multiple(
                currentImage = SelectedImage(number = 1, imageUri = "imageUri0"),
                selectedImages = listOf("imageUri0", "imageUri2", "imageUri3")
                    .map {
                        SelectedImage(number = 1, imageUri = it)
                    }
            ),
            SelectImageState.Single(
                currentImage = SelectedImage(number = 1, imageUri = "imageUri0")
            ),
        )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
internal annotation class FeedUploadScreenPreviews