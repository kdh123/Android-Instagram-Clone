package com.dhkim.home.navigation

import android.widget.Toast
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.feed.common.R
import com.dhkim.home.HomeAction
import com.dhkim.home.HomeScreen
import com.dhkim.home.HomeSideEffect
import com.dhkim.home.HomeViewModel
import com.dhkim.ui.NoticeMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

const val HOME_ROUTE = "home_route"

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.home() {
    composable(HOME_ROUTE) { backStackEntry ->
        val context = LocalContext.current
        val shouldScrollToTop = backStackEntry.savedStateHandle.get<Boolean>("extra_should_scroll_to_top") ?: false
        val viewModel = hiltViewModel<HomeViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val feeds = viewModel.feeds.collectAsLazyPagingItems()
        val likers = viewModel.likers.collectAsLazyPagingItems()
        val comments = viewModel.comments.collectAsLazyPagingItems()
        val replies by viewModel.replies.collectAsStateWithLifecycle()
        val feedState = rememberLazyListState()
        var isFeedLayoutChanged by remember { mutableStateOf(false) }
        var showNoticeMessage: String? by remember { mutableStateOf(null) }
        val pagingLoading = feeds.loadState.refresh is LoadState.Loading
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                skipHiddenState = false
            )
        )

        LaunchedEffect(shouldScrollToTop, isFeedLayoutChanged) {
            if (shouldScrollToTop && isFeedLayoutChanged) {
                feedState.animateScrollToItem(0)
            }
        }

        LaunchedEffect(viewModel) {
            viewModel.sideEffect.collectLatest {
                when (it) {
                    is HomeSideEffect.ShowToast -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }

                    HomeSideEffect.ShowRefreshFeedsFailNotice -> {
                        showNoticeMessage = context.getString(R.string.feed_refresh_failed)
                        delay(3_000)
                        showNoticeMessage = null
                    }
                }
            }
        }

        LaunchedEffect(pagingLoading) {
            if (!pagingLoading) viewModel.onAction(HomeAction.OnFeedCompleted)
        }

        LaunchedEffect(bottomSheetScaffoldState) {
            snapshotFlow { bottomSheetScaffoldState.bottomSheetState.currentValue }
                .collect { currentValue ->
                    if (currentValue == SheetValue.Hidden || currentValue == SheetValue.PartiallyExpanded) {
                        viewModel.onAction(HomeAction.DismissBottomSheet)
                    }
                }
        }

        HomeScreen(
            uiState = uiState,
            feedState = feedState,
            bottomSheetScaffoldState = bottomSheetScaffoldState,
            feeds = feeds,
            likers = likers,
            comments = comments,
            replies = replies,
            onAction = viewModel::onAction,
            onFeedLayoutChange = { isFeedLayoutChanged = it }
        )

        if (showNoticeMessage != null) {
            NoticeMessage(message = showNoticeMessage!!)
        }
    }
}