package com.dhkim.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

object InstagramTheme {
    val colors: InstagramColors
        @Composable
        get() = LocalInstagramColors.current

    val typography: InstagramTypography
        @Composable
        get() = LocalTypography.current
}

@Composable
fun InstagramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = remember(darkTheme) {
        if (darkTheme) DarkInstagramColors else LightInstagramColors
    }
    val typography = Typography

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
        }
    }

    val materialColorScheme = ColorScheme(
        primary = colors.primary,
        onPrimary = colors.onBackground,
        primaryContainer = colors.primary,
        onPrimaryContainer = colors.onBackground,
        inversePrimary = colors.primary,
        secondary = colors.primary,
        onSecondary = colors.onBackground,
        secondaryContainer = colors.background,
        onSecondaryContainer = colors.onBackground,
        tertiary = colors.textSecondary,
        onTertiary = colors.onBackground,
        tertiaryContainer = colors.surface,
        onTertiaryContainer = colors.onSurface,
        background = colors.background,
        onBackground = colors.onBackground,
        surface = colors.surface,
        onSurface = colors.onSurface,
        surfaceVariant = colors.surface,
        onSurfaceVariant = colors.onSurface,
        error = colors.error,
        onError = colors.onBackground,
        errorContainer = colors.error,
        onErrorContainer = colors.onBackground,
        outline = colors.divider,
        outlineVariant = colors.divider,
        scrim = colors.onBackground,
        inverseSurface = if (darkTheme) colors.onBackground else colors.background,
        inverseOnSurface = if (darkTheme) colors.background else colors.onBackground,
        surfaceTint = colors.primary,
    )

    CompositionLocalProvider(
        LocalInstagramColors provides colors,
        LocalTypography provides typography
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = androidx.compose.material3.Typography(),
            content = content
        )
    }
}