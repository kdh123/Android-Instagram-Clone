package com.dhkim.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.profile.ProfileScreen

const val PROFILE_ROUTE = "profile_route"

fun NavGraphBuilder.profile() {
    composable(PROFILE_ROUTE) {
        ProfileScreen()
    }
}