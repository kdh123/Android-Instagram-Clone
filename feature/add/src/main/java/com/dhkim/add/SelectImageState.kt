package com.dhkim.add

sealed interface SelectImageState {

    data class Single(
        val imageUri: String? = null
    ) : SelectImageState

    data class Multiple(
        val imageUris: List<String> = listOf()
    ) : SelectImageState
}