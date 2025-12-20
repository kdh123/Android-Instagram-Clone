package com.dhkim.add.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.add.AddScreen
import com.dhkim.add.AddViewModel

const val ADD_ROUTE = "add_route"

fun NavGraphBuilder.add() {
    composable(ADD_ROUTE) {
        val viewModel = hiltViewModel<AddViewModel>()
        AddScreen(
            onAction = viewModel::onAction
        )
    }
}