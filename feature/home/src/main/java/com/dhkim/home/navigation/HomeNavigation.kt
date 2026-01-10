package com.dhkim.home.navigation

import android.widget.Toast
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.home.HomeScreen
import com.dhkim.home.HomeSideEffect
import com.dhkim.home.HomeViewModel

const val HOME_ROUTE = "home_route"

fun NavGraphBuilder.home() {
    composable(HOME_ROUTE) { backStackEntry ->
        val context = LocalContext.current
        val shouldScrollToTop = backStackEntry.savedStateHandle.get<Boolean>("extra_should_scroll_to_top") ?: false
        val viewModel = hiltViewModel<HomeViewModel>()
        val feedUploadStatuses by viewModel.feedUploadStatuses.collectAsStateWithLifecycle()
        val feeds = viewModel.feeds.collectAsLazyPagingItems()
        val likeFeeds by viewModel.likeFeeds.collectAsStateWithLifecycle()
        val menuVisibleFeed by viewModel.menuVisibleFeed.collectAsStateWithLifecycle()
        val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
        val feedState = rememberLazyListState()
        var isFeedLayoutChanged by remember { mutableStateOf(false) }

        LaunchedEffect(shouldScrollToTop, isFeedLayoutChanged) {
            if (shouldScrollToTop && isFeedLayoutChanged) {
                feedState.animateScrollToItem(0)
            }
        }

        LaunchedEffect(viewModel) {
            viewModel.sideEffect.collect {
                when (it) {
                    is HomeSideEffect.ShowToast -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        HomeScreen(
            feedState = feedState,
            feedUploadStatuses = feedUploadStatuses,
            feeds = feeds,
            likeFeeds = likeFeeds,
            menuVisibleFeed = menuVisibleFeed,
            isNetworkAvailable = isNetworkAvailable,
            onAction = viewModel::onAction,
            onFeedLayoutChange = { isFeedLayoutChanged = it }
        )
    }
}