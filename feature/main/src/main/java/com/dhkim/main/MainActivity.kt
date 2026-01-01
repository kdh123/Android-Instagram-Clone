package com.dhkim.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.ui.eventSplash.DefaultConfig
import com.dhkim.ui.eventSplash.EventSplashApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var feedRepository: FeedRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            feedRepository.clearFeedUploadStatuses()
        }

        val splashConfig = DefaultConfig(
            appIcon = getDrawable(R.drawable.ic_app_launcher_white) ?: packageManager.getApplicationIcon(applicationInfo),
            outDuration = 1_000,
            bgColor = listOf("#405DE6", "#833AB4", "#C13584", "#F56040", "#FCAF45"),
        )

        enableEdgeToEdge()
        setContent {
            InstagramTheme {
                var hasNavigatedAfterLogin by rememberSaveable { mutableStateOf(false) }
                val appState = rememberInstagramAppState()
                val viewModel = hiltViewModel<MainViewModel>()
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                if (isLoggedIn == null) EventSplashApi.attachTo(this).with(splashConfig).show()

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn == true && !hasNavigatedAfterLogin) {
                        appState.navigateToHomeFromLogin()
                        hasNavigatedAfterLogin = true
                    }
                }

                MainScreen(appState)
            }
        }
    }
}
