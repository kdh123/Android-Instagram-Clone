package com.dhkim.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dhkim.common.handle
import com.dhkim.domain.common.useCase.GetGalleryImagesUseCase
import com.dhkim.domain.common.useCase.GetRecentGalleryImageUseCase
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.useCase.UploadFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddViewModel @Inject constructor(
    private val uploadFeedUseCase: UploadFeedUseCase,
    private val getGalleryImagesUseCase: GetGalleryImagesUseCase,
    private val getRecentGalleryImageUseCase: GetRecentGalleryImageUseCase
) : ViewModel(
) {

    private val _selectImageState = MutableStateFlow<SelectImageState>(SelectImageState.Single(null))
    val selectImageState = _selectImageState.asStateFlow()

    private val _sideEffect = Channel<AddSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    val galleryImages = getGalleryImagesUseCase(pageSize = 10)
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            val firstImage = getRecentGalleryImageUseCase().first()
            if (firstImage != null) {
                _selectImageState.value = SelectImageState.Single(firstImage.uri)
            }
        }
    }

    fun onAction(action: AddAction) {
        when (action) {
            is AddAction.ChangeSelectImageMode -> {
                changeSelectImageMode()
            }

            is AddAction.UploadFeed -> {
                uploadFeed(action.feed, action.imageUrls)
            }

            is AddAction.SelectImage -> {
                selectImage(action.imageUri)
            }
        }
    }

    private fun selectImage(selectedImageUri: String) {
        when (val currentSelectImageMode = selectImageState.value) {
            is SelectImageState.Single -> {
                _selectImageState.value = SelectImageState.Single(selectedImageUri)
            }

            is SelectImageState.Multiple -> {
                val updateSelectedImageUris = currentSelectImageMode.imageUris + selectedImageUri
                _selectImageState.value = SelectImageState.Multiple(updateSelectedImageUris)
            }
        }
    }

    private fun changeSelectImageMode() {
        when (val currentSelectImageMode = selectImageState.value) {
            is SelectImageState.Single -> {
                _selectImageState.value = SelectImageState.Multiple(imageUris = listOf(currentSelectImageMode.imageUri ?: ""))
            }

            is SelectImageState.Multiple -> {
                _selectImageState.value = SelectImageState.Single(imageUri = currentSelectImageMode.imageUris.last())
            }
        }
    }

    private fun uploadFeed(feed: Feed, imageUrls: List<String>) {
        viewModelScope.handle(
            block = {
                uploadFeedUseCase(feed = feed, imageUrls = imageUrls).first()
                _sideEffect.send(AddSideEffect.ShowToast("Feed Uploaded"))
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(AddSideEffect.ShowToast(it.message ?: "Unknown Error"))
                }
            }
        )
    }
}