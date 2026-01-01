package com.dhkim.home.navigation

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.home.HomeScreen
import com.dhkim.home.HomeViewModel

const val HOME_ROUTE = "home_route"

fun NavGraphBuilder.home() {
    composable(
        route = "$HOME_ROUTE/{shouldScrollToTop}",
        arguments = listOf(
            navArgument("shouldScrollToTop") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) {
        val shouldScrollToTop = it.arguments?.getBoolean("shouldScrollToTop") ?: false
        val viewModel = hiltViewModel<HomeViewModel>()
        val feedUploadStatuses by viewModel.feedUploadStatuses.collectAsStateWithLifecycle()
        val feeds = viewModel.feeds.collectAsLazyPagingItems()
        val feedState = rememberLazyListState()

        LaunchedEffect(shouldScrollToTop) {
            if (shouldScrollToTop) {
                feedState.animateScrollToItem(0)
            }
        }

        HomeScreen(
            feedState = feedState,
            feedUploadStatuses = feedUploadStatuses,
            feeds = feeds
        )
    }
}