package com.dhkim.reels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.dhkim.common.handle
import com.dhkim.domain.reels.useCase.GetReelsUseCase
import com.dhkim.domain.reels.useCase.PrefetchReelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class ReelsViewModel @Inject constructor(
    private val getReelsUseCase: GetReelsUseCase,
    private val prefetchReelsUseCase: PrefetchReelsUseCase,
    val mediaSourceFactory: DefaultMediaSourceFactory,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReelsUiState())
    val uiState = _uiState
        .onStart {
            initUiState()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReelsUiState()
        )

    private fun initUiState() {
        viewModelScope.handle(
            block = {
                val reels = getReelsUseCase().first().toImmutableList()
                _uiState.update {
                    ReelsUiState(contentState = ReelsContentState.Content(reels))
                }
            },
            onError = { e ->
                _uiState.update {
                    ReelsUiState(contentState = ReelsContentState.Error("${e.message}"))
                }
            }
        )
    }

    fun onAction(action: ReelsAction) {
        when (action) {
            is ReelsAction.PrefetchReels -> {
                prefetchReels(index = action.index)
            }

            is ReelsAction.SavePlaybackPosition -> {
                savePlaybackPosition(reelUrl = action.reelUrl, position = action.position)
            }

            is ReelsAction.ToggleLike -> {

            }
        }
    }

    private fun savePlaybackPosition(reelUrl: String, position: Long) {
        onContent { content ->
            val reels = content.reels.map {
                if (it.url == reelUrl) it.copy(playbackPosition = position) else it
            }.toImmutableList()
            _uiState.update {
                ReelsUiState(
                    contentState = content.copy(reels = reels)
                )
            }
        }
    }

    private fun onContent(block: (ReelsContentState.Content) -> Unit) {
        val contentState = _uiState.value.contentState
        if (contentState !is ReelsContentState.Content) return
        block(contentState)
    }

    private fun prefetchReels(index: Int) {
        val uiState = uiState.value
        if (uiState.contentState !is ReelsContentState.Content) return

        viewModelScope.handle(
            block = {
                val url = uiState.contentState.reels[index].url
                prefetchReelsUseCase(url)
            },
            onError = {

            }
        )
    }
}