package com.buildbygod.ui.plan

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GlassDialog
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.util.dayFull
import com.buildbygod.ui.util.minutesToTimeLabel

@Composable
fun PlanScreen(
    onOpenDay: (Int) -> Unit,
    vm: PlanViewModel = hiltViewModel()
) {
    val days by vm.days.collectAsStateWithLifecycle()
    var autoFillDay by remember { mutableStateOf<WorkoutDayEntity?>(null) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Weekly Plan", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Tap a day to edit, or name a day to auto-build it", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
        }
        items(days, key = { it.dayOfWeek }) { day ->
            DayPlanCard(
                day = day,
                onOpen = { onOpenDay(day.dayOfWeek) },
                onToggleReminder = { vm.setReminder(day, it) },
                onSetTime = { vm.setTime(day, it) },
                onToggleRest = { vm.setRest(day, it) },
                onEditName = { autoFillDay = day }
            )
        }
        item { Spacer(Modifier.height(90.dp)) }
    }

    autoFillDay?.let { day ->
        AutoFillDialog(
            day = day,
            previewGroups = vm::previewGroups,
            onDismiss = { autoFillDay = null },
            onRename = { name -> vm.rename(day, name); autoFillDay = null },
            onGenerate = { name -> vm.autoFill(day, name); autoFillDay = null }
        )
    }
}

@Composable
private fun AutoFillDialog(
    day: WorkoutDayEntity,
    previewGroups: (String) -> List<String>,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onGenerate: (String) -> Unit
) {
    val tokens = LocalFitTokens.current
    var text by remember { mutableStateOf(day.title) }
    val groups = previewGroups(text)
    GlassDialog(
        onDismiss = onDismiss,
        title = "Name this day",
        confirmButton = {
            TextButton(onClick = { onGenerate(text) }, enabled = text.isNotBlank()) {
                Text("Generate", color = tokens.accent)
            }
        },
        dismissButton = { TextButton(onClick = { onRename(text) }, enabled = text.isNotBlank()) { Text("Rename only", color = TextSecondary) } }
    ) {
        Text(
            "Name the day (e.g. \"Leg Day\", \"Push\", \"Arms & Abs\") and we'll auto-build warm-ups, exercises and stretches for your level.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true, label = { Text("Day name") })
        Spacer(Modifier.height(10.dp))
        if (groups.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("Targets: ${groups.joinToString(", ")}", style = MaterialTheme.typography.labelMedium, color = tokens.accent)
            }
        } else {
            Text("No muscles detected — we'll build a balanced full-body day.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun DayPlanCard(
    day: WorkoutDayEntity,
    onOpen: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSetTime: (Int) -> Unit,
    onToggleRest: (Boolean) -> Unit,
    onEditName: () -> Unit
) {
    val context = LocalContext.current
    GlassCard(Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (day.isRestDay) Brush.linearGradient(listOf(Surface2, Surface2))
                        else Brush.linearGradient(listOf(AccentBlue, AccentViolet))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dayFull(day.dayOfWeek).take(3),
                    color = if (day.isRestDay) TextSecondary else Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(day.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(6.dp))
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Rename / auto-build",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEditName() }
                    )
                }
                Text(
                    if (day.isRestDay) "Rest & recovery" else day.focus,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            if (!day.isRestDay) {
                Switch(
                    checked = day.reminderEnabled,
                    onCheckedChange = onToggleReminder,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Ink,
                        checkedTrackColor = AccentGreen,
                        uncheckedTrackColor = Surface2
                    )
                )
            }
        }

        if (!day.isRestDay) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MiniChip(
                    icon = Icons.Filled.Schedule,
                    text = minutesToTimeLabel(day.scheduledMinutes),
                    onClick = {
                        val initial = if (day.scheduledMinutes >= 0) day.scheduledMinutes else 18 * 60
                        TimePickerDialog(
                            context,
                            { _, h, m -> onSetTime(h * 60 + m) },
                            initial / 60, initial % 60, false
                        ).show()
                    }
                )
                if (day.reminderEnabled) {
                    MiniChip(icon = Icons.Filled.Notifications, text = "Reminder on", onClick = {})
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            if (day.isRestDay) "Mark as training day" else "Mark as rest day",
            style = MaterialTheme.typography.labelMedium,
            color = AccentViolet,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggleRest(!day.isRestDay) }
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun MiniChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Surface2.copy(alpha = 0.7f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}
