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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.datastore.WaterReminderPrefs
import com.buildbygod.notifications.WaterReminderScheduler
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
    onManageProfile: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val waterPrefs by vm.waterPrefs.collectAsStateWithLifecycle()
    val waterTarget by vm.waterTargetMl.collectAsStateWithLifecycle()
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
            SectionHeader("Profile")
            GlassCard(Modifier.fillMaxWidth(), onClick = onManageProfile) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ManageAccounts, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Manage profile", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Edit your name, photo, body details and goals", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Water reminder")
            WaterReminderSection(
                prefs = waterPrefs,
                targetMl = waterTarget,
                onToggle = vm::setWaterEnabled,
                onInterval = vm::setWaterInterval,
                onWindow = vm::setWaterWindow
            )

            Spacer(Modifier.height(8.dp))
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
            SectionHeader("Liquid glass")
            GlassCard(Modifier.fillMaxWidth()) {
                Text(
                    "Tune how glossy and translucent cards feel across the app.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Solid",
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
                        "Liquid",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Text(
                    "${(settings.glassIntensity * 100).toInt()}% glass",
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
private fun WaterReminderSection(
    prefs: WaterReminderPrefs,
    targetMl: Int,
    onToggle: (Boolean) -> Unit,
    onInterval: (Int) -> Unit,
    onWindow: (Int, Int) -> Unit
) {
    val tokens = LocalFitTokens.current
    val context = LocalContext.current
    val intervals = listOf(30, 45, 60, 90, 120, 180)

    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Drink water reminders", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "Goal: $targetMl ml/day, based on your body profile",
                    style = MaterialTheme.typography.labelMedium, color = TextSecondary
                )
            }
            Switch(
                checked = prefs.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Ink,
                    checkedTrackColor = tokens.accent
                )
            )
        }

        if (prefs.enabled) {
            val slots = WaterReminderScheduler.slotCount(prefs.intervalMinutes, prefs.startMinutes, prefs.endMinutes)
            val perGlass = if (slots > 0) targetMl / slots else targetMl

            Spacer(Modifier.height(14.dp))
            Text("Remind me every", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            FlowRowIntervals(intervals, prefs.intervalMinutes, tokens.accent, onInterval)

            Spacer(Modifier.height(14.dp))
            Text("Active hours", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeBox(
                    label = "From",
                    minutes = prefs.startMinutes,
                    modifier = Modifier.weight(1f)
                ) {
                    showTimePicker(context, prefs.startMinutes) { picked ->
                        val end = if (prefs.endMinutes <= picked) (picked + prefs.intervalMinutes).coerceAtMost(24 * 60 - 1) else prefs.endMinutes
                        onWindow(picked, end)
                    }
                }
                TimeBox(
                    label = "To",
                    minutes = prefs.endMinutes,
                    modifier = Modifier.weight(1f)
                ) {
                    showTimePicker(context, prefs.endMinutes) { picked ->
                        val start = if (picked <= prefs.startMinutes) (picked - prefs.intervalMinutes).coerceAtLeast(0) else prefs.startMinutes
                        onWindow(start, picked)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "About $slots reminders a day, ~$perGlass ml each.",
                style = MaterialTheme.typography.labelMedium, color = tokens.accent, fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowIntervals(
    intervals: List<Int>,
    selected: Int,
    accent: Color,
    onSelect: (Int) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        intervals.forEach { min ->
            val sel = min == selected
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (sel) accent.copy(alpha = 0.22f) else Color.Transparent)
                    .border(1.dp, if (sel) accent else TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onSelect(min) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    formatInterval(min),
                    color = if (sel) accent else TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun TimeBox(label: String, minutes: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Transparent)
            .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(formatTime(minutes), style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

private fun showTimePicker(
    context: android.content.Context,
    initialMinutes: Int,
    onPicked: (Int) -> Unit
) {
    android.app.TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(hour * 60 + minute) },
        initialMinutes / 60,
        initialMinutes % 60,
        false
    ).show()
}

private fun formatTime(minutes: Int): String {
    val h24 = (minutes / 60) % 24
    val m = minutes % 60
    val ampm = if (h24 < 12) "AM" else "PM"
    val h12 = when {
        h24 == 0 -> 12
        h24 > 12 -> h24 - 12
        else -> h24
    }
    return "%d:%02d %s".format(h12, m, ampm)
}

private fun formatInterval(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}m"
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
