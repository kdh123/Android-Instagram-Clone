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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
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
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.absoluteValue

@Composable
fun FeedUploadScreen(
    feedUploadState: FeedUploadState,
    onAction: (AddAction) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val selectedImageBitmaps = feedUploadState.selectedImageBitmaps
        val pagerState = rememberPagerState(pageCount = { selectedImageBitmaps.size })

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
                        imageModel = { selectedImageBitmaps[page].second.asAndroidBitmap() },
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
    @PreviewParameter(FeedUploadScreenPreviewParameterProvider::class) feedUploadState: FeedUploadState
) {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedUploadScreen(
                feedUploadState = feedUploadState,
                onAction = {},
                onBack = {}
            )
        }
    }
}

class FeedUploadScreenPreviewParameterProvider : PreviewParameterProvider<FeedUploadState> {

    override val values: Sequence<FeedUploadState>
        get() = sequenceOf(
            FeedUploadState(
                selectedImageBitmaps = persistentListOf(
                    1 to ImageBitmap(100, 100),
                    2 to ImageBitmap(100, 100),
                    3 to ImageBitmap(100, 100),
                )
            )
        )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
internal annotation class FeedUploadScreenPreviews