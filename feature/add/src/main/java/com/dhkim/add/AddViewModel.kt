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

    private val _selectImageMode = MutableStateFlow<SelectImageMode>(SelectImageMode.Single(null))
    val selectImageMode = _selectImageMode.asStateFlow()

    private val _sideEffect = Channel<AddSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    val galleryImages = getGalleryImagesUseCase(pageSize = 10)
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            val firstImage = getRecentGalleryImageUseCase().first()
            if (firstImage != null) {
                _selectImageMode.value = SelectImageMode.Single(firstImage.uri)
            }
        }
    }

    fun onAction(action: AddAction) {
        when (action) {
            is AddAction.UploadFeed -> {
                uploadFeed(action.feed, action.imageUrls)
            }

            is AddAction.SelectImage -> {
                when (val currentSelectImageMode = selectImageMode.value) {
                    is SelectImageMode.Single -> {
                        _selectImageMode.value = SelectImageMode.Single(action.imageUri)
                    }

                    is SelectImageMode.Multiple -> {
                        val updateSelectedImageUris = currentSelectImageMode.imageUris + action.imageUri
                        _selectImageMode.value = SelectImageMode.Multiple(updateSelectedImageUris)
                    }
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