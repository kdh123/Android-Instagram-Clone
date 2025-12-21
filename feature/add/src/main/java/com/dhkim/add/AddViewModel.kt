package com.dhkim.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.common.handle
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.useCase.UploadFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val uploadFeedUseCase: UploadFeedUseCase
) : ViewModel(
) {

    private val _sideEffect = Channel<AddSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: AddAction) {
        when (action) {
            is AddAction.UploadFeed -> {
                uploadFeed(action.feed, action.imageUrls)
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