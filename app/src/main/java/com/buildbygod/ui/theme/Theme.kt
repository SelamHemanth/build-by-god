package com.buildbygod.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun BuildByGodTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentScheme: AccentScheme = AccentScheme.AURORA,
    glassIntensity: Float = 0.65f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val tokens = if (isDark) darkTokens(accentScheme, glassIntensity)
    else lightTokens(accentScheme, glassIntensity)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = tokens.accent,
            onPrimary = Ink,
            secondary = tokens.accent2,
            onSecondary = tokens.textPrimary,
            tertiary = AccentPink,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.surfaceSolid,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.textTertiary,
            error = AccentError
        )
    } else {
        lightColorScheme(
            primary = tokens.accent,
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = tokens.accent2,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            tertiary = AccentPink,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.surfaceSolid,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.textTertiary,
            error = AccentError
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = tokens.background.toArgb()
            window.navigationBarColor = tokens.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalFitTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BuildByGodTypography,
            content = content
        )
    }
}
