package com.dhkim.add

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class FeedUploadUiState(
    val isLoading: Boolean = false,
    val selectedImageBitmaps: ImmutableList<Pair<Int, ImageBitmap>> = persistentListOf(),
    val caption: String = ""
)