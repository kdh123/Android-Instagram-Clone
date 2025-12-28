package com.dhkim.add

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.dhkim.domain.feed.model.Feed

sealed interface AddAction {

    data class SelectImage(
        val imageUri: String
    ) : AddAction

    data object ChangeSelectImageMode: AddAction

    data class UploadFeed(
        val feed: Feed,
        val imageUrls: List<String>
    ) : AddAction

    data class DragImage(
        val scale: Float,
        val offset: Offset
    ) : AddAction

    data class AddSelectedImageBitmaps(
        val imageBitmap: ImageBitmap
    ) : AddAction
}