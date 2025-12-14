package com.dhkim.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.login.LoginScreen

const val LOGIN_ROUTE = "login_route"

fun NavGraphBuilder.login() {
    composable(LOGIN_ROUTE) {
        LoginScreen()
    }
}