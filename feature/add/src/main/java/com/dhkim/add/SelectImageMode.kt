package com.dhkim.add

sealed interface SelectImageMode {

    data class Single(
        val imageUri: String? = null
    ) : SelectImageMode

    data class Multiple(
        val imageUris: List<String> = listOf()
    ) : SelectImageMode
}