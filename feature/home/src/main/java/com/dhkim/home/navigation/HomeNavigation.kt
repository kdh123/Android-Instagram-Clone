package com.dhkim.home.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.home.HomeScreen
import com.dhkim.home.HomeViewModel

const val HOME_ROUTE = "home_route"

fun NavGraphBuilder.home() {
    composable(HOME_ROUTE) {
        val viewModel = hiltViewModel<HomeViewModel>()
        val feeds = viewModel.feeds.collectAsLazyPagingItems()
        HomeScreen(
            feeds = feeds
        )
    }
}