package com.dhkim.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.eventSplash.DefaultConfig
import com.dhkim.ui.eventSplash.EventSplashApi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashConfig = DefaultConfig(
            appIcon = getDrawable(R.drawable.ic_app_launcher_white) ?: packageManager.getApplicationIcon(applicationInfo),
            outDuration = 800,
            bgColor = listOf("#405DE6", "#833AB4", "#C13584", "#F56040", "#FCAF45"),
        )

        enableEdgeToEdge()
        setContent {
            InstagramTheme {
                val appState = rememberInstagramAppState()
                val viewModel = hiltViewModel<MainViewModel>()
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                if (isLoggedIn == null) EventSplashApi.attachTo(this).with(splashConfig).show()

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn == true) {
                        appState.navigateToHomeFromLogin()
                    }
                }

                MainScreen(appState)
            }
        }
    }
}
