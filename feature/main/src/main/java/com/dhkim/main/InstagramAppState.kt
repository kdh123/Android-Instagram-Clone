package com.dhkim.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dhkim.add.navigation.ADD_ROUTE
import com.dhkim.home.navigation.HOME_ROUTE
import com.dhkim.login.navigation.LOGIN_ROUTE
import com.dhkim.profile.navigation.PROFILE_ROUTE
import com.dhkim.reels.navigation.REELS_ROUTE
import com.dhkim.search.navigation.SEARCH_ROUTE

@Stable
class InstagramAppState(
    val navController: NavHostController
) {
    val bottomItems = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Add,
        Screen.Reels,
        Screen.Profile
    )
    private val routesShowingBottomNav = listOf(
        HOME_ROUTE,
        SEARCH_ROUTE,
        REELS_ROUTE,
        PROFILE_ROUTE
    )

    val shouldShowBottomNav: Boolean
        @Composable get() {
            val entry = navController.currentBackStackEntryAsState().value
            val route = entry?.destination?.route ?: return true
            return route in routesShowingBottomNav
        }

    val currentDestination: String
        @Composable get() {
            val entry = navController.currentBackStackEntryAsState().value
            val route = entry?.destination?.parent?.route ?: entry?.destination?.route ?: return Screen.Home.route

            return route
        }

    fun navigateToHomeFromLogin() {
        navController.navigate(HOME_ROUTE) {
            popUpTo(LOGIN_ROUTE) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToHomeFromAdd() {
        navController.navigate(HOME_ROUTE) {
            popUpTo(ADD_ROUTE) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToTopLevelDestination(route: String) {
        navController.navigate(route) {
            if (!route.contains(ADD_ROUTE)) {
                popUpTo(HOME_ROUTE) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}

sealed class Screen(val imageVector: ImageVector, val route: String) {
    data object Home : Screen(Icons.Outlined.Home, HOME_ROUTE)
    data object Search : Screen(Icons.Outlined.Search, SEARCH_ROUTE)
    data object Add : Screen(Icons.Outlined.AddCircle, ADD_ROUTE)
    data object Reels : Screen(Icons.Outlined.PlayArrow, REELS_ROUTE)
    data object Profile : Screen(Icons.Outlined.Person, PROFILE_ROUTE)
}

@Composable
internal fun rememberInstagramAppState(
    navController: NavHostController = rememberNavController(),
): InstagramAppState {
    return remember(navController) {
        InstagramAppState(navController = navController)
    }
}