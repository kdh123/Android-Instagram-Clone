package com.dhkim.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.search.SearchScreen

const val SEARCH_ROUTE = "search_route"

fun NavGraphBuilder.search() {
    composable(SEARCH_ROUTE) {
        SearchScreen()
    }
}