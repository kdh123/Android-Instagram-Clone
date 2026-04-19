package com.dhkim.reels

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.reels.model.Reels
import com.dhkim.ui.LoadingSpinner
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(
    uiState: ReelsUiState,
    mediaSourceFactory: DefaultMediaSourceFactory,
    onAction: (ReelsAction) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    when (val contentState = uiState.contentState) {
        ReelsContentState.Loading -> {
            LoadingSpinner()
        }

        is ReelsContentState.Content -> {
            val pagerState = rememberPagerState(pageCount = { contentState.reels.size })

            // Pager의 현재 페이지가 변경될 때마다 Prefetch 트리거
            LaunchedEffect(pagerState.currentPage) {
                val currentPage = pagerState.currentPage

                // 다음 영상과 다다음 영상을 미리 로드 (+1, +2)
                coroutineScope.launch {
                    if (currentPage + 1 < contentState.reels.size) {
                        onAction(ReelsAction.PrefetchReels(index = currentPage + 1))
                    }
                    if (currentPage + 2 < contentState.reels.size) {
                        onAction(ReelsAction.PrefetchReels(index = currentPage + 2))
                    }
                }
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // 현재 Pager 화면에 포커스 되어 있는지 여부 판단
                val isFocused = pagerState.currentPage == page

                ReelsVideoPlayer(
                    url = contentState.reels[page].url,
                    isFocused = isFocused,
                    mediaSourceFactory = mediaSourceFactory
                )
            }
        }

        is ReelsContentState.Error -> {

        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ReelsVideoPlayer(
    url: String,
    isFocused: Boolean,
    mediaSourceFactory: DefaultMediaSourceFactory
) {
    val context = LocalContext.current

    // 캐시가 적용된 MediaSourceFactory를 사용하여 ExoPlayer 생성
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
    }

    // 포커스 상태에 따라 재생/일시정지 제어
    LaunchedEffect(isFocused) {
        if (isFocused) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
            // 필요하다면 여기서 seekTo(0)을 호출해 영상을 처음으로 되돌릴 수도 있습니다.
        }
    }

    // 컴포저블이 Dispose 될 때 Player 자원 해제
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // 릴스는 보통 기본 컨트롤러를 숨김
                // resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // 화면 꽉 차게 설정
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@ReelsScreenPreviews
@Composable
private fun ReelsScreenPreview(
    @PreviewParameter(ReelsPreviewProvider::class) uiState: ReelsUiState
) {
    val context = LocalContext.current

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ReelsScreen(
                uiState = uiState,
                mediaSourceFactory = DefaultMediaSourceFactory(context),
                onAction = {}
            )
        }
    }
}

class ReelsPreviewProvider : PreviewParameterProvider<ReelsUiState> {

    private val uiState = ReelsUiState()

    private val reels = mutableListOf<Reels>().apply {
        repeat(10) {
            add(
                Reels(
                    id = "id$it",
                    url = "url$it"
                )
            )
        }
    }.toImmutableList()

    override val values: Sequence<ReelsUiState>
        get() = sequenceOf(
            uiState,
            uiState.copy(contentState = ReelsContentState.Content(reels = reels))
        )

}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class ReelsScreenPreviews