package com.dhkim.reels.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.reels.ReelsScreen

const val REELS_ROUTE = "reels_route"

fun NavGraphBuilder.reels() {
    composable(REELS_ROUTE) {
        ReelsScreen()
    }
}