package com.dhkim.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.dhkim.common.handle
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.feed.useCase.GetLikeFeedsUseCase
import com.dhkim.domain.feed.useCase.HideFeedUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeUseCase
import com.dhkim.domain.feed.useCase.UnhideFeedUseCase
import com.dhkim.domain.feed.useCase.UpdateEnableFeedCommentUseCase
import com.dhkim.domain.feed.useCase.UpdateFeedLikeCountVisibilityUseCase
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import com.dhkim.feed.common.FeedItem
import com.dhkim.feed.common.toFeedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFeedsUseCase: GetFeedsUseCase,
    private val getFeedUploadStatusesUseCase: GetFeedUploadStatusesUseCase,
    private val updateFeedLikeCountVisibilityUseCase: UpdateFeedLikeCountVisibilityUseCase,
    private val updateEnableFeedCommentUseCase: UpdateEnableFeedCommentUseCase,
    private val hideFeedUseCase: HideFeedUseCase,
    private val unhideFeedUseCase: UnhideFeedUseCase,
    private val toggleFeedLikeUseCase: ToggleFeedLikeUseCase,
    private val getLikeFeedsUseCase: GetLikeFeedsUseCase,
    private val getUserUseCase: GetUserUseCase,
) : ViewModel() {

    val feedUploadStatuses = getFeedUploadStatusesUseCase()
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf()
        )

    val likeFeeds = getLikeFeedsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    val feeds = getFeedsUseCase()
        .map { pagingData ->
            val userId = getUserUseCase().first()?.id ?: throw NoUserFoundException()
            pagingData.map { it.toFeedItem(userId) }
        }.catch {
            emit(PagingData.empty())
        }.cachedIn(viewModelScope)

    private val _menuVisibleFeed: MutableStateFlow<FeedItem?> = MutableStateFlow(null)
    val menuVisibleFeed = _menuVisibleFeed.asStateFlow()

    private val _sideEffect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.UpdateLikeCountVisibility -> {
                updateLikeCountVisibility(action.isVisible)
            }

            is HomeAction.UpdateEnableComment -> {
                updateEnableComment(action.isEnabled)
            }

            is HomeAction.HideFeed -> {
                hideFeed(action.feedId)
            }

            is HomeAction.UnhideFeed -> {
                unhideFeed(action.feedId)
            }

            is HomeAction.ShowFeedMenu -> {
                showFeedMenu(action.feed)
            }

            HomeAction.DismissFeedMenu -> {
                dismissFeedMenu()
            }

            is HomeAction.ToggleLike -> {
                toggleLike(action.feedId)
            }
        }
    }

    private fun toggleLike(feedId: String) {
        viewModelScope.handle(
            block = {
                toggleFeedLikeUseCase(feedId)
            },
            onError = {

            }
        )
    }

    private fun dismissFeedMenu() {
        _menuVisibleFeed.update { null }
    }

    private fun showFeedMenu(feed: FeedItem) {
        _menuVisibleFeed.update { feed }
    }

    private fun updateLikeCountVisibility(isVisible: Boolean) {
        viewModelScope.handle(
            block = {
                val feedId = menuVisibleFeed.value?.feedId ?: return@handle
                updateFeedLikeCountVisibilityUseCase(feedId, isVisible)
                _menuVisibleFeed.update { it?.copy(isLikeCountVisible = isVisible) }
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.ShowToast(it.message ?: "Unknown Error"))
                }
            }
        )
    }

    private fun updateEnableComment(isEnabled: Boolean) {
        viewModelScope.handle(
            block = {
                val feedId = menuVisibleFeed.value?.feedId ?: return@handle
                updateEnableFeedCommentUseCase(feedId, isEnabled)
                _menuVisibleFeed.update { it?.copy(isCommentEnabled = isEnabled) }
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.ShowToast(it.message ?: "Unknown Error"))
                }
            }
        )
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
}