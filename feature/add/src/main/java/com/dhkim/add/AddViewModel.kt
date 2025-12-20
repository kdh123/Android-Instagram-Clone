package com.dhkim.add

import androidx.lifecycle.ViewModel
import com.dhkim.domain.feed.useCase.UploadFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val uploadFeedUseCase: UploadFeedUseCase
) : ViewModel(
) {

    fun onAction(action: AddAction) {
        when (action) {
            is AddAction.UploadFeed -> {
                uploadFeedUseCase(action.feed)
            }
        }
    }
}