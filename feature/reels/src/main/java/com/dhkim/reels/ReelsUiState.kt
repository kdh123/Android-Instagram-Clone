package com.dhkim.reels

import com.dhkim.domain.reels.model.Reels
import kotlinx.collections.immutable.ImmutableList

data class ReelsUiState(
    val contentState: ReelsContentState = ReelsContentState.Loading
)

sealed interface ReelsContentState {

    object Loading : ReelsContentState
    data class Content(val reels: ImmutableList<Reels>) : ReelsContentState
    data class Error(val message: String) : ReelsContentState
}
