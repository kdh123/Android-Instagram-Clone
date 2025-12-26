package com.dhkim.add

import androidx.compose.ui.geometry.Offset

data class ImageDragState(
    val imageUri: String,
    val offset: Offset = Offset.Zero,
    val scale: Float = 1f
)
