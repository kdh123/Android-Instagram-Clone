package com.dhkim.add

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class FeedUploadState(
    val selectedImageBitmaps: ImmutableList<Pair<Int, ImageBitmap>> = persistentListOf()
)