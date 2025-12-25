package com.dhkim.add

sealed interface SelectImageState {

    data class Single(
        val imageUri: String? = null
    ) : SelectImageState

    data class Multiple(
        val currentImageUri: String? = null,
        val selectedImages: List<SelectedImage> = listOf()
    ) : SelectImageState
}

data class SelectedImage(
    val number: Int,
    val imageUri: String
)