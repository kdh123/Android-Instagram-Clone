package com.dhkim.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dhkim.designsystem.InstagramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InstagramTheme {
                MainScreen(
                    appState = rememberInstagramAppState()
                )
            }
        }
    }
}
