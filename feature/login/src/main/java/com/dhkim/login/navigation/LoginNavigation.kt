package com.dhkim.login.navigation

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhkim.login.LoginScreen
import com.dhkim.login.LoginSideEffect
import com.dhkim.login.LoginViewModel

const val LOGIN_ROUTE = "login_route"

fun NavGraphBuilder.login(
    navigateToHome: () -> Unit
) {
    composable(LOGIN_ROUTE) {
        val viewModel = hiltViewModel<LoginViewModel>()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect {
                when (it) {
                    is LoginSideEffect.NavigateToHome -> {
                        navigateToHome()
                    }

                    is LoginSideEffect.ShowToastMessage -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        LoginScreen(
            onAction = viewModel::onAction
        )
    }
}