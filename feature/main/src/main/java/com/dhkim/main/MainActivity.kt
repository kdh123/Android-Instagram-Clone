package com.dhkim.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.eventSplash.DefaultConfig
import com.dhkim.ui.eventSplash.EventSplashApi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val splashConfig = DefaultConfig(
            appIcon = getDrawable(R.drawable.ic_app_launcher_white) ?: packageManager.getApplicationIcon(applicationInfo),
            outDuration = 1_000,
            bgColor = listOf("#405DE6", "#833AB4", "#C13584", "#F56040", "#FCAF45"),
        )
        EventSplashApi.attachTo(this).with(splashConfig).show()
        setContent {
            InstagramTheme {
                MainScreen(
                    appState = rememberInstagramAppState()
                )
            }
        }
    }
}
