package com.dhkim.main

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import com.dhkim.add.navigation.add
import com.dhkim.home.navigation.home
import com.dhkim.login.navigation.LOGIN_ROUTE
import com.dhkim.login.navigation.login
import com.dhkim.profile.navigation.profile
import com.dhkim.reels.navigation.reels
import com.dhkim.search.navigation.search

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    appState: InstagramAppState
) {
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = appState.shouldShowBottomNav,
                enter = fadeIn() + slideIn { IntOffset(0, it.height) },
                exit = fadeOut() + slideOut { IntOffset(0, it.height) }
            ) {
                if (appState.shouldShowBottomNav) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.Bottom),
                    ) {
                        appState.bottomItems.forEach { screen ->
                            val isSelected = screen.route == appState.currentDestination
                            val onBottomClick = remember { { appState.navigateToTopLevelDestination(screen.route) } }

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.imageVector,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                selected = isSelected,
                                onClick = onBottomClick
                            )
                        }
                    }
                }
            }
        }
    ) {
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding(),
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
            navController = appState.navController,
            startDestination = LOGIN_ROUTE
        ) {
            login()
            home()
            search()
            add()
            reels()
            profile()
        }
    }
}