package com.dhkim.add

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

sealed interface AddAction {

    data class SelectImage(
        val imageUri: String
    ) : AddAction

    data object ChangeSelectImageMode : AddAction

    data object UploadFeedContent : AddAction

    data class DragImage(
        val scale: Float,
        val offset: Offset
    ) : AddAction

    data class AddSelectedImageBitmaps(
        val imageBitmap: ImageBitmap
    ) : AddAction

    data class UploadFeedImages(
        val context: Context
    ) : AddAction

    data class TypeCaption(
        val text: String
    ) : AddAction
}