package com.dhkim.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.home.HomeScreen

const val HOME_ROUTE = "home_route"

fun NavGraphBuilder.home() {
    composable(HOME_ROUTE) {
        HomeScreen()
    }
}