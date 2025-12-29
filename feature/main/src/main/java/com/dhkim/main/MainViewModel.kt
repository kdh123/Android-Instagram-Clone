package com.dhkim.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.useCase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val feedRepository: FeedRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> = getUserUseCase()
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(NonCancellable) {
            feedRepository.clearFeedUploadStatuses()
        }
    }
}