package com.buildbygod.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.theme.AccentScheme
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.SectionHeader
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.theme.ThemeMode

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val tokens = LocalFitTokens.current

    Column(Modifier.fillMaxSize()) {
        GlassTopBar(title = "Settings & Appearance", onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Theme")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeModeCard(ThemeMode.SYSTEM, settings.themeMode, Icons.Filled.SettingsBrightness, Modifier.weight(1f)) { vm.setThemeMode(it) }
                ThemeModeCard(ThemeMode.LIGHT, settings.themeMode, Icons.Filled.LightMode, Modifier.weight(1f)) { vm.setThemeMode(it) }
                ThemeModeCard(ThemeMode.DARK, settings.themeMode, Icons.Filled.DarkMode, Modifier.weight(1f)) { vm.setThemeMode(it) }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Color combination")
            Text(
                "Pick an accent palette. It applies to both light and dark modes.",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AccentScheme.entries.forEach { scheme ->
                    SchemeSwatch(scheme, selected = settings.accentScheme == scheme, isDark = tokens.isDark) {
                        vm.setAccent(scheme)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Glass intensity")
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Frosted",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Slider(
                        value = settings.glassIntensity,
                        onValueChange = { vm.setGlass(it) },
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = tokens.accent,
                            activeTrackColor = tokens.accent,
                            inactiveTrackColor = tokens.surfaceSolid
                        )
                    )
                    Text(
                        "Solid",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Text(
                    "${(settings.glassIntensity * 100).toInt()}% opacity",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.accent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Text("Preview", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(tokens.accentGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Accent gradient", color = Ink, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ThemeModeCard(
    mode: ThemeMode,
    current: ThemeMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onSelect: (ThemeMode) -> Unit
) {
    val tokens = LocalFitTokens.current
    val selected = mode == current
    GlassCard(modifier, cornerRadius = 18.dp, onClick = { onSelect(mode) }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = mode.label, tint = if (selected) tokens.accent else TextSecondary)
            Spacer(Modifier.height(6.dp))
            Text(
                when (mode) {
                    ThemeMode.SYSTEM -> "System"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) TextPrimary else TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SchemeSwatch(
    scheme: AccentScheme,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalFitTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(scheme.gradient(isDark))
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) tokens.textPrimary else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Ink)
        }
        Spacer(Modifier.height(4.dp))
        Text(scheme.label, style = MaterialTheme.typography.labelMedium, color = if (selected) TextPrimary else TextSecondary)
    }
}
