package com.dhkim.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.dhkim.common.handle
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.feed.useCase.HideFeedUseCase
import com.dhkim.domain.feed.useCase.UnhideFeedUseCase
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import com.dhkim.feed.common.toFeedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFeedsUseCase: GetFeedsUseCase,
    private val getFeedUploadStatusesUseCase: GetFeedUploadStatusesUseCase,
    private val hideFeedUseCase: HideFeedUseCase,
    private val unhideFeedUseCase: UnhideFeedUseCase,
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
            val userId = getUserUseCase().first()?.id ?: throw NoUserFoundException()
            pagingData.map { it.toFeedItem(userId) }
        }.catch {
            emit(PagingData.empty())
        }.cachedIn(viewModelScope)

    private val _sideEffect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.HideFeed -> {
                hideFeed(action.feedId)
            }

            is HomeAction.UnhideFeed -> {
                unhideFeed(action.feedId)
            }
        }
    }

    private fun hideFeed(feedId: String) {
        viewModelScope.handle(
            block = {
                hideFeedUseCase(feedId).first()
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.ShowToast(it.message ?: "Unknown Error"))
                }
            }
        )
    }

    private fun unhideFeed(feedId: String) {
        viewModelScope.handle(
            block = {
                unhideFeedUseCase(feedId).first()
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.ShowToast(it.message ?: "Unknown Error"))
                }
            }
        )
    }

    override fun onCleared() {
        viewModelScope.launch(NonCancellable) {
            feedRepository.clearFeedUploadStatuses()
        }

        super.onCleared()
    }
}