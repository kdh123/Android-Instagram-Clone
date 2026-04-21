package com.dhkim.reels.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.reels.ReelsScreen
import com.dhkim.reels.ReelsViewModel

const val REELS_ROUTE = "reels_route"

fun NavGraphBuilder.reels() {
    composable(REELS_ROUTE) {
        val viewModel = hiltViewModel<ReelsViewModel>()
        val uiState = viewModel.uiState.collectAsStateWithLifecycle()

        ReelsScreen(
            uiState = uiState.value,
            onAction = viewModel::onAction
        )
    }
}