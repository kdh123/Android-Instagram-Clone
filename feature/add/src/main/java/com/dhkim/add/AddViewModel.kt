package com.dhkim.add

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dhkim.common.handle
import com.dhkim.domain.common.useCase.GetGalleryImagesUseCase
import com.dhkim.domain.common.useCase.GetRecentGalleryImageUseCase
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.useCase.UploadFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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

    val galleryImages = getGalleryImagesUseCase(pageSize = 10)
        .cachedIn(viewModelScope)

    private val _selectImageState = MutableStateFlow<SelectImageState>(SelectImageState.Single(null))
    val selectImageState = _selectImageState.asStateFlow()

    private val _feedUploadState = MutableStateFlow(FeedUploadState())
    val feedUploadState = _feedUploadState.asStateFlow()

    private val _sideEffect = Channel<AddSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val firstImage = getRecentGalleryImageUseCase().first()
            if (firstImage != null) {
                _selectImageState.value = SelectImageState.Single(
                    currentImage = SelectedImage(
                        number = 1,
                        imageUri = firstImage.uri
                    )
                )
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

            is AddAction.AddSelectedImageBitmaps -> {
                addSelectedImageBitmaps(imageBitmap = action.imageBitmap)
            }
        }
    }

    @Synchronized
    private fun addSelectedImageBitmaps(imageBitmap: ImageBitmap) {
        syncCurrentSelectedImages()
        val currentImageNumber = selectImageState.value.currentImage?.number ?: return
        val currentSelectedImageBitmaps = feedUploadState.value.selectedImageBitmaps
        val shouldUpdate = currentSelectedImageBitmaps.any { it.first == currentImageNumber }
        val updateSelectedImagesBitmaps = if (shouldUpdate) {
            currentSelectedImageBitmaps.map {
                if (it.first == currentImageNumber) {
                    currentImageNumber to imageBitmap
                } else {
                    it
                }
            }.toImmutableList()
        } else {
            (feedUploadState.value.selectedImageBitmaps + (currentImageNumber to imageBitmap))
                .distinctBy { it.first }
                .toImmutableList()
        }

        _feedUploadState.update {
            it.copy(selectedImageBitmaps = updateSelectedImagesBitmaps)
        }
    }

    private fun syncCurrentSelectedImages() {
        val currentSelectedImageNumbers = when (val selectedImageState = selectImageState.value) {
            is SelectImageState.Single -> listOf(1)
            is SelectImageState.Multiple -> selectedImageState.selectedImages.map { it.number }
        }
        _feedUploadState.update { state ->
            state.copy(
                selectedImageBitmaps = state.selectedImageBitmaps.filter {
                    currentSelectedImageNumbers.contains(it.first)
                }.toImmutableList()
            )
        }
    }

    private fun dragImage(offset: Offset, scale: Float) {
        val updateImage = selectImageState.value.currentImage?.copy(
            offset = offset,
            scale = scale
        ) ?: return

        when (val selectImageState = selectImageState.value) {
            is SelectImageState.Single -> {
                _selectImageState.update { SelectImageState.Single(updateImage) }
            }

            is SelectImageState.Multiple -> {
                val updateSelectedImages = selectImageState.selectedImages.map {
                    if (it.imageUri == updateImage.imageUri) {
                        updateImage
                    } else {
                        it
                    }
                }
                _selectImageState.update {
                    SelectImageState.Multiple(
                        currentImage = updateImage,
                        selectedImages = updateSelectedImages
                    )
                }
            }
        }
    }

    private fun selectImage(selectedImageUri: String) = viewModelScope.launch {
        when (val currentSelectImageMode = selectImageState.value) {
            is SelectImageState.Single -> {
                _selectImageState.value = SelectImageState.Single(
                    currentImage = SelectedImage(
                        number = 1,
                        imageUri = selectedImageUri
                    )
                )
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
                        focusSelectedImage(selectedImageUri)
                    }

                    // Select: Add a new image to the list and update focus
                    else -> {
                        addNewImageToSelection(selectedImageUri)
                    }
                }
            }
        }
    }

    private suspend fun addNewImageToSelection(selectedImageUri: String) {
        val currentSelectedImages = (_selectImageState.value as? SelectImageState.Multiple)?.selectedImages ?: return
        val newSelectedImage = SelectedImage(
            number = currentSelectedImages.size + 1,
            imageUri = selectedImageUri
        )

        _selectImageState.update {
            SelectImageState.Multiple(
                currentImage = newSelectedImage,
                selectedImages = currentSelectedImages + newSelectedImage
            )
        }

        _sideEffect.send(AddSideEffect.ScrollToItem(selectedImageUri))
    }

    private suspend fun focusSelectedImage(selectedImageUri: String) {
        val selectedImages = (selectImageState.value as? SelectImageState.Multiple)?.selectedImages ?: return
        val updatedCurrentImage = selectedImages.firstOrNull { it.imageUri == selectedImageUri }

        _selectImageState.update {
            SelectImageState.Multiple(
                currentImage = updatedCurrentImage,
                selectedImages = selectedImages
            )
        }

        _sideEffect.send(AddSideEffect.ScrollToItem(selectedImageUri))
    }

    private suspend fun unselectImage(unselectedImageUri: String) {
        val currentSelectedImages = (selectImageState.value as? SelectImageState.Multiple) ?: return
        val updateSelectedImages = currentSelectedImages.selectedImages
            .filter { it.imageUri != unselectedImageUri }
            .mapIndexed { index, selectedImage ->
                selectedImage.copy(number = index + 1)
            }
        val currentImage = updateSelectedImages.lastOrNull()
        _selectImageState.update {
            SelectImageState.Multiple(
                currentImage = currentImage,
                selectedImages = updateSelectedImages
            )
        }
        _sideEffect.send(AddSideEffect.ScrollToItem(currentImage?.imageUri))
    }

    private fun changeSelectImageMode() {
        viewModelScope.launch {
            when (val currentSelectImageMode = selectImageState.value) {
                is SelectImageState.Single -> {
                    _selectImageState.update {
                        SelectImageState.Multiple(
                            currentImage = currentSelectImageMode.currentImage,
                            selectedImages = listOf(currentSelectImageMode.currentImage ?: return@launch)
                        )
                    }
                    _sideEffect.send(AddSideEffect.ScrollToItem(currentSelectImageMode.currentImage?.imageUri))
                }

                is SelectImageState.Multiple -> {
                    val currentImage = currentSelectImageMode.selectedImages.lastOrNull()
                    _selectImageState.value = SelectImageState.Single(
                        currentImage = currentImage?.copy(number = 1)
                    )
                    _sideEffect.send(AddSideEffect.ScrollToItem(currentImage?.imageUri))
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