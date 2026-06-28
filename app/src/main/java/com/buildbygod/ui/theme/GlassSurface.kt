package com.buildbygod.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * The shared "liquid glass" surface treatment used across the entire app: a frosted
 * translucent base, a soft accent bloom, top + diagonal gloss highlights and a
 * gradient hairline rim. Everything scales with the global glass-intensity slider.
 *
 * @param opacityBoost makes the surface more opaque (use for chrome like the nav bar
 *        where legibility over scrolling content matters).
 * @param bloom toggles the radial accent glow (off for tiny chips).
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    opacityBoost: Float = 0f,
    bloom: Boolean = true
): Modifier {
    val tokens = LocalFitTokens.current
    val i = tokens.glassAlpha
    val fillAlpha = ((if (tokens.isDark) 0.90f - i * 0.60f else 0.96f - i * 0.44f) + opacityBoost)
        .coerceIn(0f, 1f)
    val topHi = (if (tokens.isDark) 0.06f else 0.10f) + i * 0.20f
    val sheen = (if (tokens.isDark) 0.05f else 0.08f) + i * 0.18f
    val rimHi = 0.30f + i * 0.35f
    val rimAccent = tokens.accent.copy(alpha = 0.08f + i * 0.22f)

    return this
        .clip(shape)
        .background(color = tokens.surface.copy(alpha = fillAlpha), shape = shape)
        .then(
            if (bloom) Modifier.background(
                brush = Brush.radialGradient(
                    colors = listOf(tokens.accent.copy(alpha = 0.05f + i * 0.10f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 600f
                ),
                shape = shape
            ) else Modifier
        )
        .background(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = topHi), Color.Transparent, Color.White.copy(alpha = topHi * 0.25f))
            ),
            shape = shape
        )
        .background(
            brush = Brush.linearGradient(
                0.0f to Color.White.copy(alpha = sheen),
                0.35f to Color.Transparent,
                1.0f to Color.White.copy(alpha = sheen * 0.4f)
            ),
            shape = shape
        )
        .border(
            BorderStroke(
                1.dp,
                Brush.linearGradient(listOf(Color.White.copy(alpha = rimHi), Color.Transparent, rimAccent))
            ),
            shape
        )
}

/**
 * A dialog rendered on the liquid-glass surface so popups match the rest of the app.
 * Mirrors the AlertDialog slot layout (title / content / dismiss + confirm buttons).
 */
@Composable
fun GlassDialog(
    onDismiss: () -> Unit,
    title: String? = null,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxWidth()
                .liquidGlass(RoundedCornerShape(28.dp), opacityBoost = 0.16f)
                .padding(20.dp)
        ) {
            Column {
                if (title != null) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                }
                content()
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                    if (dismissButton != null) dismissButton()
                    confirmButton()
                }
            }
        }
    }
}
