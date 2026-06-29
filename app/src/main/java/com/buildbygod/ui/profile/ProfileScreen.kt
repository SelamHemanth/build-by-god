package com.buildbygod.ui.profile

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.UnitConvert
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentPink
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.SectionHeader
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onManageProfile: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile
    val tokens = LocalFitTokens.current

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Profile", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
        }

        // ---- Identity (read-only) ----
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FramedAvatar(
                        photoPath = profile.photoUri,
                        initial = profile.name.take(1),
                        frameIndex = profile.profileFrame,
                        size = 84.dp,
                        fallbackBrush = tokens.accentGradient
                    )
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(profile.name.ifBlank { "Athlete" }, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                        Text(
                            profile.goals.joinToString(" · ") { it.label },
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.accent
                        )
                        Text(
                            "${profile.experience.label} · ${profile.sex.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // ---- Details (read-only) ----
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                DetailRow("Age", if (profile.age > 0) "${profile.age} yrs" else "—")
                DetailRow("Height", if (profile.heightCm > 0) UnitConvert.formatHeight(profile.heightCm, profile.heightUnit) else "—")
                val w = if (profile.weightKg > 0f) profile.weightKg else profile.startWeight
                DetailRow("Weight", if (w > 0f) UnitConvert.formatWeight(w, profile.weightUnit) else "—")
                DetailRow("Activity", profile.activityLevel.label)
                DetailRow("Sex", profile.sex.label, divider = false)
            }
        }

        // ---- Workout stats ----
        item { SectionHeader("Your stats") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Icons.Filled.CheckCircle, AccentGreen, "${state.stats.totalWorkouts}", "Workouts", Modifier.weight(1f))
                StatTile(Icons.Filled.LocalFireDepartment, AccentAmber, "${state.stats.streak}", "Day streak", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Icons.Filled.Timer, AccentBlue, "${state.stats.totalMinutes}", "Total minutes", Modifier.weight(1f))
                StatTile(Icons.Filled.Bolt, AccentPink, "${state.stats.totalCaloriesBurned}", "Calories burned", Modifier.weight(1f))
            }
        }

        // ---- Settings & controls (below profile, no top button) ----
        item { SectionHeader("Settings & controls") }
        item {
            ActionRow(
                icon = Icons.Filled.ManageAccounts,
                title = "Manage profile",
                subtitle = "Edit your name, photo, body details and goals",
                onClick = onManageProfile
            )
        }
        item {
            ActionRow(
                icon = Icons.Filled.Settings,
                title = "Settings & appearance",
                subtitle = "Theme, accent colours and liquid glass",
                onClick = onOpenSettings
            )
        }
        item { NotificationsCard() }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Build By God  ·  v0.2.0", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text("powered by hemanth", style = MaterialTheme.typography.labelMedium, color = tokens.accent, fontWeight = FontWeight.SemiBold)
            Text("All data stays on your device.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, divider: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
    if (divider) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TextSecondary.copy(alpha = 0.12f))
        )
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val tokens = LocalFitTokens.current
    GlassCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun NotificationsCard() {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val tokens = LocalFitTokens.current
    GlassCard(Modifier.fillMaxWidth(), onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Enable workout notifications", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Allow Build By God to remind you when it's time to train.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, tint: Color, value: String, label: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}
