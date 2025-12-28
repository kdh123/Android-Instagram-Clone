package com.dhkim.add

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.LoadingSpinner
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun FeedUploadScreen(
    uiState: FeedUploadUiState,
    onAction: (AddAction) -> Unit,
    onBack: () -> Unit
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TopBar(onBack = onBack)
            FeedImages(selectedImageBitmaps = uiState.selectedImageBitmaps)
            FeedCaption(
                caption = uiState.caption,
                onAction = onAction,
                modifier = Modifier
                    .height(0.dp)
                    .weight(1f)
            )
            FeedUploadButton(onAction = onAction)
        }

        if (uiState.isLoading) {
            LoadingSpinner(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun FeedUploadButton(
    onAction: (AddAction) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(10.dp)
            .clickable { onAction(AddAction.UploadFeed) }
    ) {
        Text(
            text = stringResource(R.string.share),
            textAlign = TextAlign.Center,
            style = InstagramTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .background(color = InstagramTheme.colors.primary)
                .padding(8.dp)
        )
    }
}

@Composable
fun FeedCaption(
    caption: String,
    onAction: (AddAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        BasicTextField(
            value = caption,
            onValueChange = { onAction(AddAction.TypeCaption(it)) },
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = InstagramTheme.colors.onBackground,
                lineHeight = 24.sp
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (caption.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_caption),
                            style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun FeedImages(selectedImageBitmaps: ImmutableList<Pair<Int, ImageBitmap>>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            items = selectedImageBitmaps,
            key = { it.first }
        ) { item ->
            GlideImage(
                imageModel = { item.second.asAndroidBitmap() },
                previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
                imageOptions = ImageOptions(contentScale = ContentScale.FillWidth),
                modifier = Modifier
                    .width(300.dp)
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun TopBar(
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
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
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
    }
}

@FeedUploadScreenPreviews
@Composable
private fun FeedUploadScreenPreview(
    @PreviewParameter(FeedUploadScreenPreviewParameterProvider::class) feedUploadUiState: FeedUploadUiState
) {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedUploadScreen(
                uiState = feedUploadUiState,
                onAction = {},
                onBack = {}
            )
        }
    }
}

class FeedUploadScreenPreviewParameterProvider : PreviewParameterProvider<FeedUploadUiState> {

    override val values: Sequence<FeedUploadUiState>
        get() = sequenceOf(
            FeedUploadUiState(
                caption = "",
                selectedImageBitmaps = persistentListOf(
                    1 to ImageBitmap(100, 100),
                    2 to ImageBitmap(100, 100),
                    3 to ImageBitmap(100, 100),
                )
            ),
            FeedUploadUiState(
                caption = "Hello World!",
                selectedImageBitmaps = persistentListOf(
                    1 to ImageBitmap(100, 100),
                    2 to ImageBitmap(100, 100),
                    3 to ImageBitmap(100, 100),
                )
            ),
            FeedUploadUiState(
                caption = "Hello World!\nHello World!\nHello World!\nHello World!\nHello World!\n",
                selectedImageBitmaps = persistentListOf(
                    1 to ImageBitmap(100, 100),
                    2 to ImageBitmap(100, 100),
                    3 to ImageBitmap(100, 100),
                )
            ),
            FeedUploadUiState(
                isLoading = true,
                caption = "Hello World!\nHello World!\nHello World!\nHello World!\nHello World!\n",
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