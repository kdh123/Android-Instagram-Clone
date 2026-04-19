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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class ReelsViewModel @Inject constructor(
    private val getReelsUseCase: GetReelsUseCase,
    private val prefetchReelsUseCase: PrefetchReelsUseCase,
    val mediaSourceFactory: DefaultMediaSourceFactory,
) : ViewModel() {

    val uiState: StateFlow<ReelsUiState> = getReelsUseCase()
        .map { reels ->
            ReelsUiState(
                contentState = ReelsContentState.Content(reels.toImmutableList())
            )
        }.catch {
            emit(ReelsUiState(contentState = ReelsContentState.Error(it.message.toString())))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReelsUiState()
        )

    fun onAction(action: ReelsAction) {
        when (action) {
            is ReelsAction.PrefetchReels -> {
                prefetchReels(index = action.index)
            }
        }
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