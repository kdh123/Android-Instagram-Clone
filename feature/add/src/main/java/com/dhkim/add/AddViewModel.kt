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
        viewModelScope.launch {
            when (val currentSelectImageMode = selectImageState.value) {
                is SelectImageState.Single -> {
                    _selectImageState.value = SelectImageState.Single(selectedImageUri)
                    _sideEffect.send(AddSideEffect.ScrollToItem(selectedImageUri))
                }

                is SelectImageState.Multiple -> {
                    val currentSelectedImages = currentSelectImageMode.selectedImages
                    val isAlreadySelected = currentSelectedImages.any { it.imageUri == selectedImageUri }
                    val isCurrentFocused = currentSelectImageMode.currentImageUri == selectedImageUri
                    val shouldUnselect = currentSelectedImages.size > 1 && isCurrentFocused

                    when {
                        // Already focused image is clicked and multiple items are selected
                        shouldUnselect -> {
                            unselectImage(unselectedImageUri = selectedImageUri)
                        }

                        // Update the current image focus if it's already in the selection list
                        isAlreadySelected -> {
                            _selectImageState.value = SelectImageState.Multiple(
                                currentImageUri = selectedImageUri,
                                selectedImages = currentSelectedImages
                            )
                            _sideEffect.send(AddSideEffect.ScrollToItem(selectedImageUri))
                        }

                        // Select: Add a new image to the list and update focus
                        else -> {
                            val newSelectedImages = currentSelectedImages + SelectedImage(
                                number = currentSelectedImages.size + 1,
                                imageUri = selectedImageUri
                            )
                            _selectImageState.value = SelectImageState.Multiple(
                                currentImageUri = selectedImageUri,
                                selectedImages = newSelectedImages
                            )
                            _sideEffect.send(AddSideEffect.ScrollToItem(selectedImageUri))
                        }
                    }
                }
            }
        }
    }

    private suspend fun unselectImage(unselectedImageUri: String) {
        val currentSelectedImages = (selectImageState.value as? SelectImageState.Multiple) ?: return
        val updateSelectedImages = currentSelectedImages.selectedImages
            .filter { it.imageUri != unselectedImageUri }
            .mapIndexed { index, selectedImage ->
                selectedImage.copy(number = index + 1)
            }
        val currentImageUri = updateSelectedImages.lastOrNull()?.imageUri
        _selectImageState.value = SelectImageState.Multiple(
            currentImageUri = currentImageUri,
            selectedImages = updateSelectedImages
        )
        _sideEffect.send(AddSideEffect.ScrollToItem(currentImageUri ?: ""))
    }

    private fun changeSelectImageMode() {
        when (val currentSelectImageMode = selectImageState.value) {
            is SelectImageState.Single -> {
                val image = SelectedImage(number = 1, imageUri = currentSelectImageMode.imageUri ?: "")
                _selectImageState.value = SelectImageState.Multiple(currentImageUri = image.imageUri, selectedImages = listOf(image))
            }

            is SelectImageState.Multiple -> {
                _selectImageState.value = SelectImageState.Single(imageUri = currentSelectImageMode.selectedImages.map { it.imageUri }.lastOrNull())
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