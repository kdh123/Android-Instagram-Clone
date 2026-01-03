package com.dhkim.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.user.useCase.GetUserUseCase
import com.dhkim.feed.common.toFeedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFeedsUseCase: GetFeedsUseCase,
    private val getFeedUploadStatusesUseCase: GetFeedUploadStatusesUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val feedRepository: FeedRepository,
) : ViewModel() {

    val feedUploadStatuses = getFeedUploadStatusesUseCase()
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf()
        )

    val feeds = getFeedsUseCase()
        .map { pagingData ->
            val userId = getUserUseCase().first()?.id ?: ""
            pagingData.map {
                it.toFeedItem(myUserId = userId)
            }
        }.cachedIn(viewModelScope)

    override fun onCleared() {
        viewModelScope.launch(NonCancellable) {
            feedRepository.clearFeedUploadStatuses()
        }

        super.onCleared()
    }
}