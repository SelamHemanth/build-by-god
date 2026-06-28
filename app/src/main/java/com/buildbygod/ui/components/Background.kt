package com.buildbygod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.buildbygod.ui.theme.LocalFitTokens

/** Full-screen glossy background with soft radial accent glows, adapts to theme. */
@Composable
fun GlossyBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalFitTokens.current
    // Richer backdrop as glass intensity rises, so the translucency has color to reveal.
    val base = if (tokens.isDark) 0.16f else 0.10f
    val glow = base + tokens.glassAlpha * 0.16f
    Box(
        modifier
            .fillMaxSize()
            .background(tokens.background)
            .background(
                Brush.radialGradient(
                    colors = listOf(tokens.accent2.copy(alpha = glow), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 950f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(tokens.accent.copy(alpha = glow), Color.Transparent),
                    center = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    radius = 1150f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(tokens.accent.copy(alpha = glow * 0.5f), Color.Transparent),
                    center = Offset(Float.POSITIVE_INFINITY, 0f),
                    radius = 700f
                )
            )
    ) {
        content()
    }
}
