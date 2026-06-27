package com.buildbygod.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Fixed semantic accents (used for icons / muscle groups / status). These stay
// constant across light & dark so they must be vivid enough for both.
// ---------------------------------------------------------------------------
val AccentViolet = Color(0xFF6C5CE7)
val AccentBlue = Color(0xFF00B8E6)
val AccentPink = Color(0xFFFF5CAA)
val AccentGreen = Color(0xFF14C79A)
val AccentAmber = Color(0xFFF5A623)
val AccentError = Color(0xFFFF5A5F)

// On-accent ink: text/icons drawn on top of a bright accent gradient. Dark in
// both themes (bright gradients always need dark foreground).
val Ink = Color(0xFF0B0B0F)

// Glass overlay tints (subtle, work on both themes).
val GlassFillTop = Color(0x1FFFFFFF)
val GlassFillBottom = Color(0x0AFFFFFF)

fun glassGradient() = Brush.verticalGradient(colors = listOf(GlassFillTop, GlassFillBottom))

// ---------------------------------------------------------------------------
// Theme configuration enums
// ---------------------------------------------------------------------------
enum class ThemeMode(val label: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/** Selectable accent color combinations applied across the whole app. */
enum class AccentScheme(
    val label: String,
    private val darkPrimary: Long,
    private val darkSecondary: Long,
    private val lightPrimary: Long,
    private val lightSecondary: Long
) {
    AURORA("Aurora", 0xFF00D2FF, 0xFF6C5CE7, 0xFF2563EB, 0xFF7C3AED),
    OCEAN("Ocean", 0xFF22D3EE, 0xFF3B82F6, 0xFF0891B2, 0xFF1D4ED8),
    SUNSET("Sunset", 0xFFFF9A5A, 0xFFFF5CAA, 0xFFEA580C, 0xFFDB2777),
    FOREST("Forest", 0xFF34D399, 0xFF10B981, 0xFF059669, 0xFF0D9488),
    GRAPE("Grape", 0xFFB794F6, 0xFFEC4899, 0xFF7C3AED, 0xFFDB2777),
    CRIMSON("Crimson", 0xFFFF7A7A, 0xFFFF9A5A, 0xFFDC2626, 0xFFEA580C);

    fun primary(isDark: Boolean) = Color(if (isDark) darkPrimary else lightPrimary)
    fun secondary(isDark: Boolean) = Color(if (isDark) darkSecondary else lightSecondary)
    fun gradient(isDark: Boolean) = Brush.linearGradient(listOf(primary(isDark), secondary(isDark)))

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: AURORA
    }
}

// ---------------------------------------------------------------------------
// Resolved palette tokens, provided via CompositionLocal so the whole tree
// reacts to theme-mode / accent / glass-intensity changes at runtime.
// ---------------------------------------------------------------------------
data class FitTokens(
    val isDark: Boolean,
    val scheme: AccentScheme,
    val background: Color,
    val surface: Color,        // glass base
    val surfaceSolid: Color,   // solid chips / tracks
    val sheet: Color,          // bottom sheets / dialogs
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val stroke: Color,
    val accent: Color,
    val accent2: Color,
    val accentGradient: Brush,
    val glassAlpha: Float
)

fun darkTokens(scheme: AccentScheme, glassAlpha: Float) = FitTokens(
    isDark = true,
    scheme = scheme,
    background = Color(0xFF0B0B0F),
    surface = Color(0xFF1A1B23),
    surfaceSolid = Color(0xFF22232E),
    sheet = Color(0xFF15151D),
    textPrimary = Color(0xFFF4F5FB),
    textSecondary = Color(0xFFA7A9BC),
    textTertiary = Color(0xFF6E7088),
    stroke = Color(0x33FFFFFF),
    accent = scheme.primary(true),
    accent2 = scheme.secondary(true),
    accentGradient = scheme.gradient(true),
    glassAlpha = glassAlpha
)

fun lightTokens(scheme: AccentScheme, glassAlpha: Float) = FitTokens(
    isDark = false,
    scheme = scheme,
    background = Color(0xFFF1F4FA),
    surface = Color(0xFFFFFFFF),
    surfaceSolid = Color(0xFFE6EAF3),
    sheet = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF0E1020),
    textSecondary = Color(0xFF54586C),
    textTertiary = Color(0xFF8A8EA3),
    stroke = Color(0x14101020),
    accent = scheme.primary(false),
    accent2 = scheme.secondary(false),
    accentGradient = scheme.gradient(false),
    glassAlpha = glassAlpha
)

val LocalFitTokens = staticCompositionLocalOf { darkTokens(AccentScheme.AURORA, 0.65f) }

// ---------------------------------------------------------------------------
// Adaptive token accessors. These keep the original names that screens already
// import, but now resolve from the current theme at composition time.
// ---------------------------------------------------------------------------
val TextPrimary: Color @Composable get() = LocalFitTokens.current.textPrimary
val TextSecondary: Color @Composable get() = LocalFitTokens.current.textSecondary
val TextTertiary: Color @Composable get() = LocalFitTokens.current.textTertiary
val Surface1: Color @Composable get() = LocalFitTokens.current.surface
val Surface2: Color @Composable get() = LocalFitTokens.current.surfaceSolid
val InkElevated: Color @Composable get() = LocalFitTokens.current.sheet
val GlassStroke: Color @Composable get() = LocalFitTokens.current.stroke
val AccentGradient: Brush @Composable get() = LocalFitTokens.current.accentGradient
