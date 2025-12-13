package com.dhkim.add.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.add.AddScreen

const val ADD_ROUTE = "add_route"

fun NavGraphBuilder.add() {
    composable(ADD_ROUTE) {
        AddScreen()
    }
}