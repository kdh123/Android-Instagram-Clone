package com.dhkim.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.dhkim.common.RestartableStateFlow
import com.dhkim.common.handle
import com.dhkim.common.restartableStateIn
import com.dhkim.domain.feed.model.Reply
import com.dhkim.domain.feed.useCase.AddCommentUseCase
import com.dhkim.domain.feed.useCase.DeleteCommentUseCase
import com.dhkim.domain.feed.useCase.GetCommentsUseCase
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.feed.useCase.GetLikeFeedsUseCase
import com.dhkim.domain.feed.useCase.GetLikersUseCase
import com.dhkim.domain.feed.useCase.GetMyFeedsUseCase
import com.dhkim.domain.feed.useCase.GetRepliesUseCase
import com.dhkim.domain.feed.useCase.HideFeedUseCase
import com.dhkim.domain.feed.useCase.ReplyCommentUseCase
import com.dhkim.domain.feed.useCase.ToggleEnableCommentUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeCountVisibilityUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeUseCase
import com.dhkim.domain.feed.useCase.UnhideFeedUseCase
import com.dhkim.domain.user.exception.NoUserFoundException
import com.dhkim.domain.user.useCase.GetUserUseCase
import com.dhkim.feed.common.CommentItem
import com.dhkim.feed.common.FeedItem
import com.dhkim.feed.common.ReplyGroup
import com.dhkim.feed.common.toFeedItem
import com.dhkim.network.ConnectivityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val getLikersUseCase: GetLikersUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val getRepliesUseCase: GetRepliesUseCase,
    private val replyCommentUseCase: ReplyCommentUseCase,
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

    private val targetFeedForLikers: MutableStateFlow<FeedItem?> = MutableStateFlow(null)
    val likers = targetFeedForLikers.flatMapLatest { feedItem ->
        if (feedItem != null) {
            getLikersUseCase(feedItem.feedId).map { pagingData ->
                pagingData.map { it.toUserItem() }
            }
        } else {
            flowOf(PagingData.empty())
        }
    }.cachedIn(viewModelScope)

    private val targetCommentForReply: MutableStateFlow<CommentItem?> = MutableStateFlow(null)
    private val recentAddedReply: MutableStateFlow<Reply?> = MutableStateFlow(null)

    private val targetFeedForComments: MutableStateFlow<FeedItem?> = MutableStateFlow(null)
    private val refreshCommentTrigger: MutableStateFlow<Int> = MutableStateFlow(0)
    val comments = combine(
        targetFeedForComments,
        refreshCommentTrigger,
    ) { feedItem, _ ->
        feedItem
    }.flatMapLatest { feedItem ->
        if (feedItem != null) {
            getCommentsUseCase(feedItem.feedId).map { pagingData ->
                pagingData.map { it.toCommentItem() }
            }
        } else {
            flowOf(PagingData.empty())
        }
    }.cachedIn(viewModelScope)

    private val menuVisibleFeed: MutableStateFlow<FeedItem?> = MutableStateFlow(null)
    private val isRefreshing = MutableStateFlow(false)

    val uiState: RestartableStateFlow<HomeUiState> = isRefreshing.flatMapLatest { isRefreshing ->
        combine(
            getMyFeedsUseCase(),
            getFeedUploadStatusesUseCase(),
            getLikeFeedsUseCase(),
            menuVisibleFeed,
            connectivityChecker.isNetworkAvailable(),
        ) { myFeeds, feedUploadStatuses, likeFeeds, menuVisibleFeed, isNetworkAvailable ->
            HomeUiState(
                isRefreshing = isRefreshing,
                myFeeds = myFeeds.toImmutableSet(),
                feedUploadStatuses = feedUploadStatuses.toImmutableList(),
                likeFeeds = likeFeeds.toImmutableSet(),
                menuVisibleFeed = menuVisibleFeed,
                isNetworkAvailable = isNetworkAvailable
            )
        }.combine(targetFeedForLikers) { uiState, targetFeedForLikers ->
            uiState.copy(targetFeedForLikers = targetFeedForLikers)
        }.combine(targetFeedForComments) { uiState, targetFeedForComments ->
            uiState.copy(targetFeedForComments = targetFeedForComments)
        }.combine(getUserUseCase()) { uiState, user ->
            uiState.copy(userProfileImageUrl = user?.profileUrl ?: "")
        }
    }.restartableStateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private val _replies = MutableStateFlow<ImmutableList<ReplyGroup>>(persistentListOf())
    val replies = _replies.asStateFlow()

    private val _sideEffect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        observeServerReplies()
        observeLocalReplyUpdates()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ToggleLikeCountVisibility -> {
                toggleLikeCountVisibility()
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
                toggleEnableComment()
            }

            HomeAction.DismissBottomSheet -> {
                dismissBottomSheet()
            }

            is HomeAction.ShowLikers -> {
                showLikers(action.feedItem)
            }

            is HomeAction.ShowComments -> {
                showComments(action.feedItem)
            }

            is HomeAction.AddComment -> {
                addComment(action.comment)
            }

            is HomeAction.DeleteComment -> {
                deleteComment(action.comment)
            }

            is HomeAction.ShowReplies -> {
                showReplies(action.comment)
            }

            is HomeAction.ReplyComment -> {
                replyComment(action.comment, action.content)
            }
        }
    }

    private fun deleteComment(comment: CommentItem) {
        viewModelScope.handle(
            block = {
                val feedId = targetFeedForComments.value?.feedId ?: return@handle
                deleteCommentUseCase(feedId, comment.commentId)
                refreshCommentTrigger.update { it + 1 }
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowRefreshFeedsFailNotice)
            }
        )
    }

    private fun observeServerReplies() {
        viewModelScope.launch {
            targetCommentForReply
                .filterNotNull()
                .flatMapLatest { comment -> getRepliesUseCase(comment.commentId) }
                .collect { serverReplies ->
                    if (serverReplies.isNotEmpty()) {
                        updateRepliesFromServer(serverReplies)
                    }
                }
        }
    }

    private fun observeLocalReplyUpdates() {
        viewModelScope.launch {
            recentAddedReply
                .filterNotNull()
                .collect { newReply ->
                    addLocalReplyToGroup(newReply)
                }
        }
    }

    private fun updateRepliesFromServer(serverReplies: List<Reply>) {
        _replies.update { currentList ->
            val commentId = serverReplies.first().commentId
            val existingGroup = currentList.find { it.commentId == commentId }

            val newGroup = ReplyGroup(
                commentId = commentId,
                replies = serverReplies.map { it.toReplyItem() }.toImmutableList(),
                recentAddedReplies = existingGroup?.recentAddedReplies ?: persistentListOf()
            )

            currentList.upsert(newGroup)
        }
    }

    private fun addLocalReplyToGroup(newReply: Reply) {
        _replies.update { currentList ->
            val targetIndex = currentList.indexOfFirst { it.commentId == newReply.commentId }
            if (targetIndex != -1) {
                // Case 1: Group already exists
                val targetGroup = currentList[targetIndex]
                if (targetGroup.hasLocalReply(newReply.replyId)) return@update currentList

                val updatedGroup = targetGroup.copy(
                    recentAddedReplies = (targetGroup.recentAddedReplies + newReply.toReplyItem()).toImmutableList()
                )
                currentList.updateItem(targetIndex, updatedGroup)
            } else {
                // Case 2: Group does not exist (first reply or server delay)
                val newGroup = ReplyGroup(
                    commentId = newReply.commentId,
                    replies = persistentListOf(), // 서버 데이터는 아직 없으므로 빈 리스트
                    recentAddedReplies = persistentListOf(newReply.toReplyItem()) // 내 답글만 넣음
                )
                (currentList + persistentListOf(newGroup)).toImmutableList()
            }
        }
    }

    private fun showReplies(comment: CommentItem) {
        targetCommentForReply.update { comment }
    }

    private fun replyComment(comment: CommentItem, content: String) {
        viewModelScope.handle(
            block = {
                targetCommentForReply.update { comment }
                val feedId = targetFeedForComments.value?.feedId ?: return@handle
                val reply = replyCommentUseCase(feedId, comment.commentId, content)
                recentAddedReply.update { reply }
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowRefreshFeedsFailNotice)
            }
        )
    }

    private fun showComments(feedItem: FeedItem) {
        targetFeedForComments.update { feedItem }
    }

    private fun addComment(comment: String) {
        viewModelScope.handle(
            block = {
                val feedId = targetFeedForComments.value?.feedId ?: return@handle
                addCommentUseCase(feedId, comment)
                refreshCommentTrigger.update { it + 1 }
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowRefreshFeedsFailNotice)
            }
        )
    }

    private fun showLikers(feedItem: FeedItem) {
        targetFeedForLikers.update { feedItem }
    }

    private fun dismissBottomSheet() {
        targetFeedForLikers.update { null }
        targetFeedForComments.update { null }
        targetCommentForReply.update { null }
        recentAddedReply.update { null }
        _replies.update { persistentListOf() }
    }

    private fun toggleEnableComment() {
        viewModelScope.handle(
            block = {
                val feedId = menuVisibleFeed.value?.feedId ?: return@handle
                val isEnabled = menuVisibleFeed.value?.isCommentEnabled ?: return@handle
                toggleEnableCommentUseCase(feedId, isEnabled)
                menuVisibleFeed.update { it?.copy(isCommentEnabled = !isEnabled) }
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowRefreshFeedsFailNotice)
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
                val isLiked = uiState.value.likeFeeds.any { it.feedId == feedId }
                toggleFeedLikeUseCase(feedId, isLiked)
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowRefreshFeedsFailNotice)
            }
        )
    }

    private fun dismissFeedMenu() {
        menuVisibleFeed.update { null }
    }

    private fun showFeedMenu(feed: FeedItem) {
        menuVisibleFeed.update { feed }
    }

    private fun toggleLikeCountVisibility() {
        viewModelScope.handle(
            block = {
                val feedId = menuVisibleFeed.value?.feedId ?: return@handle
                val isVisible = menuVisibleFeed.value?.isLikeCountVisible ?: return@handle
                toggleFeedLikeCountVisibilityUseCase(feedId, isVisible)
                menuVisibleFeed.update { it?.copy(isLikeCountVisible = !isVisible) }
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowToast(it.message ?: "Unknown Error"))
            }
        )
    }

    private fun hideFeed(feedId: String) {
        viewModelScope.handle(
            block = {
                hideFeedUseCase(feedId).first()
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowToast(it.message ?: "Unknown Error"))
            }
        )
    }

    private fun unhideFeed(feedId: String) {
        viewModelScope.handle(
            block = {
                unhideFeedUseCase(feedId).first()
            },
            onError = {
                _sideEffect.send(HomeSideEffect.ShowToast(it.message ?: "Unknown Error"))
            }
        )
    }

    private fun ImmutableList<ReplyGroup>.upsert(item: ReplyGroup): ImmutableList<ReplyGroup> {
        val currentList = toMutableList()
        val index = indexOfFirst { it.commentId == item.commentId }
        if (index >= 0) {
            currentList[index] = item
        } else {
            currentList.add(item)
        }
        return currentList.toImmutableList()
    }

    private fun ImmutableList<ReplyGroup>.updateItem(index: Int, item: ReplyGroup): ImmutableList<ReplyGroup> {
        val currentList = toMutableList()
        currentList[index] = item
        return currentList.toImmutableList()
    }

    private fun ReplyGroup.hasLocalReply(replyId: String): Boolean {
        return recentAddedReplies.any { it.replyId == replyId }
    }
}