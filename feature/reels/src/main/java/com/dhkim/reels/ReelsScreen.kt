package com.dhkim.reels

import android.content.res.Configuration
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
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
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.reels.model.Reel
import com.dhkim.ui.LoadingSpinner
import com.dhkim.ui.noRippleClick
import com.dhkim.video.VideoCacheManager
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(
    uiState: ReelsUiState,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsContent(
    reels: ImmutableList<Reel>,
    onAction: (ReelsAction) -> Unit,
) {
    val pagerState = rememberPagerState { reels.size }
    val coroutineScope = rememberCoroutineScope()
    var isMuted by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val sheetMaxHeightPx = with(density) { 400.dp.toPx() }
    var sheetOffsetPx by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        sheetOffsetPx = (sheetOffsetPx - delta).coerceIn(0f, sheetMaxHeightPx)
    }
    val commentHeight = with(density) { sheetOffsetPx.toDp() }

    LaunchedEffect(key1 = pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
            pagerState.animateScrollToPage(page)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            VerticalPager(
                state = pagerState,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { index ->
                val shouldPlay by remember(pagerState) {
                    derivedStateOf {
                        pagerState.currentPage == index || pagerState.targetPage == index
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!LocalInspectionMode.current) {
                        ReelPlayer(
                            reel = reels[index],
                            shouldPlay = shouldPlay,
                            isMuted = isMuted,
                            isScrolling = pagerState.isScrollInProgress,
                            onMuted = { isMuted = it },
                            onDoubleTap = { onAction(ReelsAction.ToggleLike(reelUrl = reels[index].url)) },
                            onAction = onAction
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        ReelOptions(
                            onCommentClick = {
                                coroutineScope.launch {
                                    animate(
                                        initialValue = sheetOffsetPx,
                                        targetValue = sheetMaxHeightPx,
                                        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)
                                    ) { value, _ -> sheetOffsetPx = value }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 8.dp)
                        )
                        ReelProfile(
                            userName = reels[index].userName,
                            profileImage = reels[index].userProfileImage,
                            caption = "Hello World!",
                            onProfileClick = {},
                            modifier = Modifier
                        )
                    }
                }
            }
        }
        CommentBottomSheet(
            draggableState = draggableState,
            onDragStopped = { velocity ->
                val target = if (sheetOffsetPx < sheetMaxHeightPx * 0.5f || velocity > 500f) {
                    0f
                } else {
                    sheetMaxHeightPx
                }
                animate(
                    initialValue = sheetOffsetPx,
                    targetValue = target,
                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)
                ) { value, _ -> sheetOffsetPx = value }
            },
            modifier = Modifier
                .height(commentHeight.coerceAtLeast(0.dp))
        )
    }
}

@Composable
fun ReelProgressBar(
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartProgress by remember { mutableFloatStateOf(0f) }
    var accumulatedDragPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isDragging) {
                val duration = exoPlayer.duration
                currentProgress = if (duration > 0) {
                    exoPlayer.currentPosition.toFloat() / duration.toFloat()
                } else {
                    0f
                }
            }
            delay(50)
        }
    }

    LinearProgressIndicator(
        progress = { currentProgress },
        color = Color.LightGray,
        strokeCap = StrokeCap.Square,
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragStartProgress = currentProgress
                        accumulatedDragPx = 0f
                        exoPlayer.pause()
                    },
                    onDragEnd = {
                        isDragging = false
                        val duration = exoPlayer.duration
                        if (duration > 0) {
                            exoPlayer.seekTo((currentProgress * duration).toLong())
                        }
                        exoPlayer.play()
                    },
                    onDragCancel = {
                        isDragging = false
                        currentProgress = dragStartProgress
                        exoPlayer.play()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDragPx += dragAmount.x
                        currentProgress = (dragStartProgress + accumulatedDragPx / size.width)
                            .coerceIn(0f, 1f)
                    }
                )
            },
    )
}

@Composable
fun ReelOptions(
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Icon(
            painter = painterResource(R.drawable.ic_comment),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
                .noRippleClick(onClick = onCommentClick)
        )
    }
}

@Composable
fun CommentBottomSheet(
    draggableState: DraggableState,
    onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = onDragStopped
                )
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.5f))
            )
        }
        Text(
            text = "댓글",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ReelProfile(
    userName: String,
    profileImage: String,
    caption: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            GlideImage(
                imageModel = { profileImage },
                previewPlaceholder = painterResource(com.dhkim.ui.R.drawable.ic_dummy_background),
                modifier = modifier
                    .clip(CircleShape)
                    .size(36.dp)
                    .noRippleClick(onClick = onProfileClick)
            )
            Text(
                text = userName,
                style = InstagramTheme.typography.labelMediumBold,
                modifier = Modifier
            )
        }
        Text(
            text = caption,
            style = InstagramTheme.typography.bodyMedium,
            modifier = Modifier
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberExoPlayerWithLifecycle(
    reelUrl: String,
    position: Long
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
            if (position > 0) seekTo(position)
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
    onAction: (ReelsAction) -> Unit
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

        ReelProgressBar(
            exoPlayer = exoPlayer,
            modifier = Modifier.align(Alignment.BottomCenter)
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
            onAction(ReelsAction.SavePlaybackPosition(reelUrl = reel.url, position = exoPlayer.currentPosition))
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
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ReelsScreen(
                uiState = uiState,
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
                    url = "url$it",
                    caption = "caption$it",
                    likeCount = it,
                    userId = "userId$it",
                    userName = "userName$it",
                    userProfileImage = "userProfileImage$it",
                    commentCount = 0,
                    isLiked = it % 2 == 0
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
