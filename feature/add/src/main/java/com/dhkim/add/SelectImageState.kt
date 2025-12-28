package com.dhkim.add

import androidx.compose.ui.geometry.Offset

sealed class SelectImageState(
    open val currentImage: SelectedImage?
) {

    data class Single(
        override val currentImage: SelectedImage?
    ) : SelectImageState(currentImage)

    data class Multiple(
        override val currentImage: SelectedImage?,
        val selectedImages: List<SelectedImage> = listOf()
    ) : SelectImageState(currentImage)
}

data class SelectedImage(
    val number: Int,
    val imageUri: String,
    val offset: Offset = Offset.Zero,
    val scale: Float = 1f
)