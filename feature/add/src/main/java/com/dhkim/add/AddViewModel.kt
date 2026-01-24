package com.dhkim.add

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dhkim.add.work.UploadFeedContentWorker
import com.dhkim.add.work.UploadFeedImagesWorker
import com.dhkim.common.handle
import com.dhkim.domain.common.useCase.GetGalleryImagesUseCase
import com.dhkim.domain.common.useCase.GetRecentGalleryImageUseCase
import com.dhkim.domain.feed.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddViewModel @Inject constructor(
    private val getGalleryImagesUseCase: GetGalleryImagesUseCase,
    private val getRecentGalleryImageUseCase: GetRecentGalleryImageUseCase,
    private val feedRepository: FeedRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val galleryImages = refreshTrigger.flatMapLatest {
        getGalleryImagesUseCase(pageSize = 20)
            .cachedIn(viewModelScope)
    }

    private val _selectImageState = MutableStateFlow<SelectImageState>(SelectImageState.Single(null))
    val selectImageState = _selectImageState.asStateFlow()

    private val _feedUploadUiState = MutableStateFlow(FeedUploadUiState())
    val feedUploadUiState = _feedUploadUiState.asStateFlow()

    private val _sideEffect = Channel<AddSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private val feedId: String = "feedId_${System.currentTimeMillis()}"

    init {
        refreshTrigger.flatMapLatest {
            getRecentGalleryImageUseCase().onEach { firstImage ->
                if (firstImage != null) {
                    _selectImageState.value = SelectImageState.Single(
                        currentImage = SelectedImage(
                            number = 1,
                            imageUri = firstImage.uri
                        )
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: AddAction) {
        when (action) {
            is AddAction.ChangeSelectImageMode -> {
                changeSelectImageMode()
            }

            is AddAction.UploadFeedContent -> {
                uploadFeedContent()
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

            is AddAction.UploadFeedImages -> {
                viewModelScope.launch {
                    uploadFeedImages(context = action.context)
                    _sideEffect.send(AddSideEffect.NavigateToFeedUpload)
                }
            }

            AddAction.RefreshGalleryImages -> {
                refreshGalleryImages()
            }
        }
    }

    private fun refreshGalleryImages() {
        refreshTrigger.update { it + 1 }
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
                        if (currentSelectedImages.size < 10) {
                            addNewImageToSelection(selectedImageUri)
                        } else {
                            _sideEffect.send(AddSideEffect.ShowImagesLimitedNotice)
                        }
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

    private fun uploadFeedContent() {
        viewModelScope.handle(
            block = {
                if (feedUploadUiState.value.isLoading) return@handle
                val caption = feedUploadUiState.value.caption
                val inputData = workDataOf(
                    "KEY_FEED_ID" to feedId,
                    "KEY_FEED_CAPTION" to caption,
                )
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadFeedContentWorker>()
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .build()

                updateFeedUploadStatus()
                workManager.enqueue(uploadWorkRequest)
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

    private fun updateFeedUploadStatus() = viewModelScope.launch {
        val currentFeedUploadStatus = feedRepository.getFeedUploadStatus(feedId).first() ?: return@launch
        val updateFeedUploadStatus = currentFeedUploadStatus.copy(shouldUpload = true)
        feedRepository.insertFeedUploadStatus(updateFeedUploadStatus)
    }

    private suspend fun saveImageBitmapToCache(context: Context): List<String?> {
        return withContext(Dispatchers.IO) {
            val imageBitmaps = feedUploadUiState.value.selectedImageBitmaps
            imageBitmaps.map { it.second }
                .map { imageBitmap ->
                    val androidBitmap = imageBitmap.asAndroidBitmap()
                    val fileName = "feed_upload_${UUID.randomUUID()}.jpg"
                    val cacheFile = File(context.cacheDir, fileName)
                    try {
                        FileOutputStream(cacheFile).use { out ->
                            androidBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            out.flush()
                        }
                        cacheFile.absolutePath
                    } catch (_: Exception) {
                        null
                    }
                }
        }
    }

    private suspend fun uploadFeedImages(context: Context) = withContext(Dispatchers.IO) {
        val imageUris = saveImageBitmapToCache(context).toTypedArray()
        val inputData = workDataOf(
            "KEY_FEED_ID" to feedId,
            "KEY_IMAGE_URIS" to imageUris,
        )
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadFeedImagesWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(uploadWorkRequest)
    }
}