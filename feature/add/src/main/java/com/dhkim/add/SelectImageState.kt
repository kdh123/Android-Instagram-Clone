package com.dhkim.add

import androidx.compose.ui.geometry.Offset

sealed interface SelectImageState {

    data class Single(
        val imageUri: String? = null
    ) : SelectImageState

    data class Multiple(
        val currentImage: SelectedImage? = null,
        val selectedImages: List<SelectedImage> = listOf()
    ) : SelectImageState
}

data class SelectedImage(
    val number: Int,
    val imageUri: String,
    val offset: Offset = Offset.Zero,
    val scale: Float = 1f
)