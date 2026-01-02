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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.feed.model.UploadState
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.home.navigation.HOME_ROUTE
import com.dhkim.main.work.RemoveUploadCompletedFeedUploadStateWorker
import com.dhkim.ui.eventSplash.DefaultConfig
import com.dhkim.ui.eventSplash.EventSplashApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var feedRepository: FeedRepository

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashConfig = DefaultConfig(
            appIcon = getDrawable(R.drawable.ic_app_launcher_white) ?: packageManager.getApplicationIcon(applicationInfo),
            outDuration = 1_000,
            bgColor = listOf("#405DE6", "#833AB4", "#C13584", "#F56040", "#FCAF45"),
        )

        enableEdgeToEdge()
        setContent {
            InstagramTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
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

                LaunchedEffect(Unit) {
                    lifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            combine(
                                appState.navController.currentBackStackEntryFlow,
                                feedRepository.getFeedUploadStatuses()
                            ) { backStackEntry, feedUploadStatuses ->
                                val isAtHome = backStackEntry.destination.route == HOME_ROUTE
                                val uploadCompletedFeedIds = feedUploadStatuses
                                    .filter { it.uploadState == UploadState.COMPLETE }
                                    .map { it.feedId }
                                isAtHome to uploadCompletedFeedIds
                            }.collect {
                                val (isAtHome, uploadCompletedFeedIds) = it
                                if (isAtHome) {
                                    uploadCompletedFeedIds.forEach(::removeUploadCompletedFeedUploadState)
                                }
                            }
                        }
                    }
                }

                MainScreen(appState)
            }
        }
    }

    private fun removeUploadCompletedFeedUploadState(feedId: String) {
        val inputData = workDataOf(
            "KEY_FEED_ID" to feedId
        )
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest = OneTimeWorkRequestBuilder<RemoveUploadCompletedFeedUploadStateWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(uploadWorkRequest)
    }
}
