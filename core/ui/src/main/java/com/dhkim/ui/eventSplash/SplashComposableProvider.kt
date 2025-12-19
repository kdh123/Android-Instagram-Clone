package com.dhkim.ui.eventSplash

import androidx.compose.runtime.Composable

fun interface SplashComposableProvider {

    @Composable
    fun Content(onFinish: () -> Unit)
}