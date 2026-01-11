package com.dhkim.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.dhkim.common.RestartableStateFlow
import com.dhkim.common.handle
import com.dhkim.common.restartableStateIn
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.feed.useCase.GetLikeFeedsUseCase
import com.dhkim.domain.feed.useCase.GetMyFeedsUseCase
import com.dhkim.domain.feed.useCase.HideFeedUseCase
import com.dhkim.domain.feed.useCase.ToggleEnableCommentUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeUseCase
import com.dhkim.domain.feed.useCase.UnhideFeedUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeCountVisibilityUseCase
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import com.dhkim.feed.common.FeedItem
import com.dhkim.feed.common.toFeedItem
import com.dhkim.network.ConnectivityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMyFeedsUseCase: GetMyFeedsUseCase,
    private val getFeedsUseCase: GetFeedsUseCase,
    private val getFeedUploadStatusesUseCase: GetFeedUploadStatusesUseCase,
    private val toggleFeedLikeCountVisibilityUseCase: ToggleFeedLikeCountVisibilityUseCase,
    private val hideFeedUseCase: HideFeedUseCase,
    private val unhideFeedUseCase: UnhideFeedUseCase,
    private val toggleFeedLikeUseCase: ToggleFeedLikeUseCase,
    private val toggleEnableCommentUseCase: ToggleEnableCommentUseCase,
    private val getLikeFeedsUseCase: GetLikeFeedsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val connectivityChecker: ConnectivityChecker
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val feeds = refreshTrigger.flatMapLatest {
        getFeedsUseCase()
            .map { pagingData ->
                val userId = getUserUseCase().first()?.id ?: throw NoUserFoundException()
                pagingData.map { it.toFeedItem(userId) }
            }.catch {
                emit(PagingData.empty())
            }.cachedIn(viewModelScope)
    }

    private val menuVisibleFeed: MutableStateFlow<FeedItem?> = MutableStateFlow(null)
    private val isRefreshing = MutableStateFlow(false)

    val uiState: RestartableStateFlow<HomeUiState> = isRefreshing.flatMapLatest { isRefreshing ->
        combine(
            getMyFeedsUseCase(),
            getFeedUploadStatusesUseCase(),
            getLikeFeedsUseCase(),
            menuVisibleFeed,
            connectivityChecker.isNetworkAvailable()
        ) { myFeeds, feedUploadStatuses, likeFeeds, menuVisibleFeed, isNetworkAvailable ->
            HomeUiState(
                isRefreshing = isRefreshing,
                myFeeds = myFeeds.toImmutableSet(),
                feedUploadStatuses = feedUploadStatuses.toImmutableList(),
                likeFeeds = likeFeeds.toImmutableSet(),
                menuVisibleFeed = menuVisibleFeed,
                isNetworkAvailable = isNetworkAvailable
            )
        }
    }.restartableStateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private val _sideEffect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ToggleLikeCountVisibility -> {
                toggleLikeCountVisibility(action.isVisible)
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

            HomeAction.RefreshFeeds -> {
                refreshFeeds()
            }

            HomeAction.OnFeedCompleted -> {
                onFeedCompleted()
            }

            is HomeAction.ToggleEnableComment -> {
                toggleEnableComment(action.isEnabled)
            }
        }
    }

    private fun toggleEnableComment(isEnabled: Boolean) {
        viewModelScope.handle(
            block = {
                val feedId = menuVisibleFeed.value?.feedId ?: return@handle
                toggleEnableCommentUseCase(feedId)
                menuVisibleFeed.update { it?.copy(isCommentEnabled = isEnabled) }
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.ShowRefreshFeedsFailNotice)
                }
            }
        )
    }

    private fun onFeedCompleted() {
        isRefreshing.update { false }
    }

    private fun refreshFeeds() = viewModelScope.launch {
        val isNetworkAvailable = uiState.value.isNetworkAvailable
        isRefreshing.update { true }
        if (isNetworkAvailable) {
            refreshTrigger.update { it + 1 }
        } else {
            delay(100)
            isRefreshing.update { false }
            _sideEffect.send(HomeSideEffect.ShowRefreshFeedsFailNotice)
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
        menuVisibleFeed.update { null }
    }

    private fun showFeedMenu(feed: FeedItem) {
        menuVisibleFeed.update { feed }
    }

    private fun toggleLikeCountVisibility(isVisible: Boolean) {
        viewModelScope.handle(
            block = {
                val feedId = menuVisibleFeed.value?.feedId ?: return@handle
                toggleFeedLikeCountVisibilityUseCase(feedId)
                menuVisibleFeed.update { it?.copy(isLikeCountVisible = isVisible) }
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