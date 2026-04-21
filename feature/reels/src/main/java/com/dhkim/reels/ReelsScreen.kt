package com.dhkim.reels

import android.content.res.Configuration
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.reels.model.Reel
import com.dhkim.ui.LoadingSpinner
import com.dhkim.video.VideoCacheManager
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(
    uiState: ReelsUiState,
    mediaSourceFactory: DefaultMediaSourceFactory,
    onAction: (ReelsAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        when (val contentState = uiState.contentState) {
            ReelsContentState.Loading -> {
                LoadingSpinner(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(10.dp)
                )
            }

            is ReelsContentState.Content -> {
                ReelsContent(
                    reels = contentState.reels,
                    onAction = onAction
                )
            }

            is ReelsContentState.Error -> {

            }
        }
    }

}

@Composable
fun ReelsContent(
    reels: ImmutableList<Reel>,
    onAction: (ReelsAction) -> Unit,
) {
    val pagerState = rememberPagerState { reels.size }
    var isMuted by remember {
        mutableStateOf(false)
    }
    val isFirstItem by remember(pagerState) {
        derivedStateOf {
            pagerState.currentPage == 0
        }
    }

    LaunchedEffect(key1 = pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
            pagerState.animateScrollToPage(page)
        }
    }

    Box {
        VerticalPager(
            state = pagerState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { index ->
            val shouldPlay by remember(pagerState) {
                derivedStateOf {
                    pagerState.currentPage == index || pagerState.targetPage == index
                }
            }
            ReelPlayer(
                reel = reels[index],
                shouldPlay = shouldPlay,
                isMuted = isMuted,
                isScrolling = pagerState.isScrollInProgress,
                onMuted = {
                    isMuted = it
                },
                onDoubleTap = {
                    onAction(ReelsAction.ToggleLike(reelUrl = reels[index].url))
                },
                savePlaybackPosition = {
                    onAction(ReelsAction.SavePlaybackPosition(reels[index].url, it))
                }
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberExoPlayerWithLifecycle(
    reelUrl: String,
    position: Long,
): ExoPlayer {

    val context = LocalContext.current
    val exoPlayer = remember(reelUrl) {
        // 1. 전역 캐시 가져오기
        val cache = VideoCacheManager.getCache(context)

        // 2. 네트워크용 DataSource 생성 (캐시에 데이터가 없을 때 사용)
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()

        // 3. 캐시와 네트워크를 결합한 DataSource 생성
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        ExoPlayer.Builder(context).build().apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            repeatMode = Player.REPEAT_MODE_ONE
            setHandleAudioBecomingNoisy(true)

            // 4. 캐시 팩토리를 사용하여 MediaSource 생성
            val source = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(reelUrl))

            setMediaSource(source)
            prepare()
            if (position > 0) {
                seekTo(position)
            }
        }
    }
    var appInBackground by remember {
        mutableStateOf(false)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, appInBackground) {
        val lifecycleObserver = getExoPlayerLifecycleObserver(exoPlayer, appInBackground) {
            appInBackground = it
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return exoPlayer
}

fun getExoPlayerLifecycleObserver(
    exoPlayer: ExoPlayer,
    wasAppInBackground: Boolean,
    setWasAppInBackground: (Boolean) -> Unit
): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (wasAppInBackground)
                    exoPlayer.playWhenReady = true
                setWasAppInBackground(false)
            }

            Lifecycle.Event.ON_PAUSE -> {
                exoPlayer.playWhenReady = false
                setWasAppInBackground(true)
            }

            Lifecycle.Event.ON_STOP -> {
                exoPlayer.playWhenReady = false
                setWasAppInBackground(true)
            }

            Lifecycle.Event.ON_DESTROY -> {
                exoPlayer.release()
            }

            else -> {}
        }
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReelPlayer(
    reel: Reel,
    shouldPlay: Boolean,
    isMuted: Boolean,
    onMuted: (Boolean) -> Unit,
    onDoubleTap: (Boolean) -> Unit,
    isScrolling: Boolean,
    savePlaybackPosition: (Long) -> Unit
) {
    val exoPlayer = rememberExoPlayerWithLifecycle(reel.url, reel.playbackPosition)
    val playerView = rememberPlayerView(exoPlayer)
    var volumeIconVisibility by remember { mutableStateOf(false) }
    var likeIconVisibility by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box {
        AndroidView(
            factory = { playerView },
            modifier = Modifier
                .pointerInput(reel.isLiked, isMuted) {
                    detectTapGestures(
                        onDoubleTap = {
                            onDoubleTap(true)
                            coroutineScope.launch {
                                likeIconVisibility = true
                                delay(800)
                                likeIconVisibility = false
                            }
                        },
                        onTap = {
                            if (exoPlayer.playWhenReady) {
                                if (isMuted.not()) {
                                    exoPlayer.volume = 0f
                                    onMuted(true)
                                } else {
                                    exoPlayer.volume = 1f
                                    onMuted(false)
                                }
                                coroutineScope.launch {
                                    volumeIconVisibility = true
                                    delay(800)
                                    volumeIconVisibility = false
                                }
                            }
                        },
                        /*onPress = {
                            if (!isScrolling) {
                                exoPlayer.playWhenReady = false
                                awaitRelease()
                                exoPlayer.playWhenReady = true
                            }
                        },*/
                        onLongPress = {}
                    )
                },
            update = {
                exoPlayer.playWhenReady = shouldPlay
                exoPlayer.volume = if (isMuted) 0f else 1f
            }
        )

        AnimatedVisibility(
            visible = likeIconVisibility,
            enter = scaleIn(
                spring(Spring.DampingRatioMediumBouncy)
            ),
            exit = scaleOut(tween(150)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.White.copy(0.90f),
                modifier = Modifier
                    .size(100.dp)
            )
        }

        if (volumeIconVisibility) {
            /*Icon(
                imageVector = Icons.Outlined.,
                contentDescription = null,
                tint = Color.White.copy(0.75f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
            )*/
        }

    }

    DisposableEffect(key1 = true) {
        onDispose {
            savePlaybackPosition(exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberPlayerView(exoPlayer: ExoPlayer): PlayerView {
    val context = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            player = exoPlayer
            setShowBuffering(SHOW_BUFFERING_ALWAYS)
        }
    }
    DisposableEffect(key1 = true) {
        onDispose {
            playerView.player = null
        }
    }
    return playerView
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

    private val reels = mutableListOf<Reel>().apply {
        repeat(10) {
            add(
                Reel(
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