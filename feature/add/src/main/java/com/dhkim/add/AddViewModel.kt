package com.dhkim.add

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dhkim.common.handle
import com.dhkim.domain.common.useCase.GetGalleryImagesUseCase
import com.dhkim.domain.common.useCase.GetRecentGalleryImageUseCase
import com.dhkim.domain.feed.useCase.ImageDownloadUrl
import com.dhkim.domain.feed.useCase.UploadFeedUseCase
import com.dhkim.domain.feed.useCase.UploadImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddViewModel @Inject constructor(
    private val uploadImageUseCase: UploadImageUseCase,
    private val uploadFeedUseCase: UploadFeedUseCase,
    private val getGalleryImagesUseCase: GetGalleryImagesUseCase,
    private val getRecentGalleryImageUseCase: GetRecentGalleryImageUseCase
) : ViewModel(
) {

    val galleryImages = getGalleryImagesUseCase(pageSize = 10)
        .cachedIn(viewModelScope)

    private val _selectImageState = MutableStateFlow<SelectImageState>(SelectImageState.Single(null))
    val selectImageState = _selectImageState.asStateFlow()

    private val _feedUploadUiState = MutableStateFlow(FeedUploadUiState())
    val feedUploadUiState = _feedUploadUiState.asStateFlow()

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
                uploadFeed()
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

            is AddAction.TypeCaption -> {
                typeCaption(text = action.text)
            }
        }
    }

    private fun typeCaption(text: String) {
        _feedUploadUiState.update {
            it.copy(caption = text)
        }
    }

    @Synchronized
    private fun addSelectedImageBitmaps(imageBitmap: ImageBitmap) {
        syncCurrentSelectedImages()
        val currentImageNumber = selectImageState.value.currentImage?.number ?: return
        val currentSelectedImageBitmaps = feedUploadUiState.value.selectedImageBitmaps
        val shouldUpdate = currentSelectedImageBitmaps.any { it.first == currentImageNumber }
        val updateSelectedImagesBitmaps = if (shouldUpdate) {
            currentSelectedImageBitmaps.map {
                if (it.first == currentImageNumber) {
                    currentImageNumber to imageBitmap
                } else {
                    it
                }
            }.sortedBy {
                it.first
            }.toImmutableList()
        } else {
            (feedUploadUiState.value.selectedImageBitmaps + (currentImageNumber to imageBitmap))
                .distinctBy { it.first }
                .sortedBy { it.first }
                .toImmutableList()
        }

        _feedUploadUiState.update {
            it.copy(selectedImageBitmaps = updateSelectedImagesBitmaps)
        }
    }

    private fun syncCurrentSelectedImages() {
        val currentSelectedImageNumbers = when (val selectedImageState = selectImageState.value) {
            is SelectImageState.Single -> listOf(1)
            is SelectImageState.Multiple -> selectedImageState.selectedImages.map { it.number }
        }
        _feedUploadUiState.update { state ->
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

    private fun uploadFeed() {
        viewModelScope.handle(
            block = {
                if (feedUploadUiState.value.isLoading) return@handle

                _feedUploadUiState.update { it.copy(isLoading = true) }
                val imageDownloadUrls = uploadFeedImages()
                val caption = feedUploadUiState.value.caption
                uploadFeedUseCase(caption = caption, imageUrls = imageDownloadUrls).first()
                _feedUploadUiState.update { it.copy(isLoading = false) }
                _sideEffect.send(AddSideEffect.NavigateToHome)
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(AddSideEffect.ShowToast(it.message ?: "Unknown Error"))
                    _feedUploadUiState.update { state -> state.copy(isLoading = false) }
                }
            }
        )
    }

    context(CoroutineScope)
    private suspend fun uploadFeedImages(): List<ImageDownloadUrl> {
        val uploadImageJobs = mutableListOf<Deferred<ImageDownloadUrl>>()
        val imageBitmaps = feedUploadUiState.value.selectedImageBitmaps
        for (imageBitmap in imageBitmaps) {
            val baos = ByteArrayOutputStream()
            imageBitmap.second.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val job = async {
                uploadImageUseCase(baos.toByteArray()).first()
            }
            uploadImageJobs.add(job)
        }
        return uploadImageJobs.awaitAll()
    }
}