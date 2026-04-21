package com.dhkim.reels

import com.dhkim.domain.reels.model.Reel
import kotlinx.collections.immutable.ImmutableList

data class ReelsUiState(
    val contentState: ReelsContentState = ReelsContentState.Loading
)

sealed interface ReelsContentState {

    object Loading : ReelsContentState
    data class Content(val reels: ImmutableList<Reel>) : ReelsContentState
    data class Error(val message: String) : ReelsContentState
}
