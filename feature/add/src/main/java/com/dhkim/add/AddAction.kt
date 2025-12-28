package com.dhkim.add

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

sealed interface AddAction {

    data class SelectImage(
        val imageUri: String
    ) : AddAction

    data object ChangeSelectImageMode : AddAction

    data object UploadFeed : AddAction

    data class DragImage(
        val scale: Float,
        val offset: Offset
    ) : AddAction

    data class AddSelectedImageBitmaps(
        val imageBitmap: ImageBitmap
    ) : AddAction

    data class TypeCaption(
        val text: String
    ) : AddAction
}