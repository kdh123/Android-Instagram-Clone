package com.dhkim.add

import androidx.compose.ui.geometry.Offset
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

    private val imageDragStates = mutableListOf<ImageDragState>()

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
                selectImage(selectedImageUri = action.imageUri)
            }

            is AddAction.DragImage -> {
                dragImage(offset = action.offset, scale = action.scale)
            }

            is AddAction.AddImage -> {}
        }
    }

    private fun dragImage(offset: Offset, scale: Float) {
        val currentImageUri = (selectImageState.value as? SelectImageState.Multiple)?.currentImage?.imageUri ?: return
        val imageDragState = imageDragStates.find { it.imageUri == currentImageUri } ?: return
        imageDragStates[imageDragStates.indexOf(imageDragState)] = imageDragState.copy(offset = offset, scale = scale)
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
                    val isCurrentFocused = currentSelectImageMode.currentImage?.imageUri == selectedImageUri
                    val shouldUnselect = currentSelectedImages.size > 1 && isCurrentFocused

                    when {
                        // Already focused image is clicked and multiple items are selected
                        shouldUnselect -> {
                            unselectImage(unselectedImageUri = selectedImageUri)
                        }

                        // Update the current image focus if it's already in the selection list
                        isAlreadySelected -> {
                            val currentImageDragState = imageDragStates.find { it.imageUri == selectedImageUri } ?: return@launch
                            _selectImageState.value = SelectImageState.Multiple(
                                currentImage = currentSelectedImages
                                    .firstOrNull { it.imageUri == selectedImageUri }
                                    ?.copy(
                                        offset = currentImageDragState.offset,
                                        scale = currentImageDragState.scale
                                    ),
                                selectedImages = currentSelectedImages
                            )
                            _sideEffect.send(AddSideEffect.ScrollToItem(selectedImageUri))
                        }

                        // Select: Add a new image to the list and update focus
                        else -> {
                            val newSelectedImage = SelectedImage(
                                number = currentSelectedImages.size + 1,
                                imageUri = selectedImageUri
                            )
                            val newSelectedImages = currentSelectedImages + newSelectedImage
                            _selectImageState.value = SelectImageState.Multiple(
                                currentImage = newSelectedImage,
                                selectedImages = newSelectedImages
                            )
                            imageDragStates.add(ImageDragState(imageUri = selectedImageUri))
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
                val imageDragState = imageDragStates.find { it.imageUri == selectedImage.imageUri }
                selectedImage.copy(
                    number = index + 1,
                    offset = imageDragState?.offset ?: Offset.Zero,
                    scale = imageDragState?.scale ?: 1f
                )
            }
        val currentImage = updateSelectedImages.lastOrNull()
        _selectImageState.value = SelectImageState.Multiple(
            currentImage = currentImage,
            selectedImages = updateSelectedImages
        )
        _sideEffect.send(AddSideEffect.ScrollToItem(currentImage?.imageUri))
    }

    private fun changeSelectImageMode() {
        viewModelScope.launch {
            when (val currentSelectImageMode = selectImageState.value) {
                is SelectImageState.Single -> {
                    val currentImage = SelectedImage(number = 1, imageUri = currentSelectImageMode.imageUri ?: "")
                    _selectImageState.value = SelectImageState.Multiple(currentImage = currentImage, selectedImages = listOf(currentImage))
                    imageDragStates.add(ImageDragState(imageUri = currentImage.imageUri))
                    _sideEffect.send(AddSideEffect.ScrollToItem(currentImage.imageUri))
                }

                is SelectImageState.Multiple -> {
                    val currentImageUri = currentSelectImageMode.selectedImages.map { it.imageUri }.lastOrNull()
                    _selectImageState.value = SelectImageState.Single(imageUri = currentImageUri)
                    _sideEffect.send(AddSideEffect.ScrollToItem(currentImageUri))
                }
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