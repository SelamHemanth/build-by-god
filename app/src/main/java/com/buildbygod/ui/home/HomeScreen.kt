package com.buildbygod.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.ui.profile.FramedAvatar
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGradient
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GlassDialog
import com.buildbygod.ui.theme.GradientButton
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.ProgressRing
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.util.dayShort
import com.buildbygod.ui.util.minutesToTimeLabel
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    onOpenDay: (Int) -> Unit,
    onStartSession: (Int) -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenDiet: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var renaming by remember { mutableStateOf(false) }
    var wellnessTip by remember { mutableStateOf<WellnessMove?>(null) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(greeting(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(
                        state.name.ifBlank { "Athlete" },
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                FramedAvatar(
                    photoPath = state.photoUri,
                    initial = state.name.take(1),
                    frameIndex = state.profileFrame,
                    size = 52.dp,
                    fallbackBrush = AccentGradient
                )
            }
        }

        state.activeSession?.let { active ->
            item { ResumeCard(active, onResume = { onStartSession(active.day) }) }
        }

        item { TodayCard(state, onOpenDay, onStartSession, onRename = { renaming = true }) }

        item { DietCard(onOpenDiet) }

        item {
            Text("Your week", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.week.forEach { day ->
                    WeekDot(day = day, isToday = day.dayOfWeek == state.todayDow, modifier = Modifier.weight(1f)) {
                        onOpenDay(day.dayOfWeek)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = { Icon(Icons.Filled.LocalFireDepartment, null, tint = AccentAmber) },
                    value = "${state.streak}",
                    label = "Day streak",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = { Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen) },
                    value = "${state.totalSessions}",
                    label = "Workouts done",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Breathe & recover", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            BreathingCard(onOpenBreathing)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                wellnessMoves.forEach { move ->
                    WellnessRow(move) { wellnessTip = move }
                }
            }
        }

        item {
            DailyTipCard()
            Spacer(Modifier.height(90.dp))
        }
    }

    wellnessTip?.let { move ->
        GlassDialog(
            onDismiss = { wellnessTip = null },
            title = move.title,
            confirmButton = { TextButton(onClick = { wellnessTip = null }) { Text("Got it", color = LocalFitTokens.current.accent) } }
        ) {
            move.steps.forEachIndexed { i, step ->
                Text("${i + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                if (i < move.steps.lastIndex) Spacer(Modifier.height(6.dp))
            }
        }
    }

    if (renaming) {
        val today = state.today
        RenameDayDialog(
            initial = today?.title.orEmpty(),
            suggestion = state.suggestedName,
            onDismiss = { renaming = false },
            onConfirm = { vm.renameToday(it); renaming = false }
        )
    }
}

@Composable
private fun ResumeCard(active: com.buildbygod.data.datastore.ActiveSession, onResume: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), onClick = onResume) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(AccentGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = Ink, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Workout in progress", style = MaterialTheme.typography.labelMedium, color = AccentGreen, fontWeight = FontWeight.Bold)
                Text(
                    active.title.ifBlank { "Your workout" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                val mins = (active.elapsedSeconds() / 60).toInt()
                Text(
                    "${active.completed}/${active.total} done · ${mins} min" + if (active.paused) " · paused" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            Text("Resume", style = MaterialTheme.typography.titleMedium, color = AccentBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RenameDayDialog(
    initial: String,
    suggestion: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val tokens = LocalFitTokens.current
    var text by remember { mutableStateOf(initial) }
    GlassDialog(
        onDismiss = onDismiss,
        title = "Rename today",
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Save", color = tokens.accent)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            label = { Text("Day name") }
        )
        if (suggestion.isNotBlank() && suggestion != text) {
            Spacer(Modifier.height(10.dp))
            Text("Suggested from today's exercises:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                suggestion,
                style = MaterialTheme.typography.titleMedium,
                color = tokens.accent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { text = suggestion }
            )
        }
    }
}

private data class WellnessMove(val title: String, val subtitle: String, val icon: ImageVector, val steps: List<String>)

private val wellnessMoves = listOf(
    WellnessMove(
        "Posture reset", "Undo desk slouch in 30s", Icons.Filled.SelfImprovement,
        listOf(
            "Sit or stand tall, roll your shoulders back and down.",
            "Tuck your chin slightly and lengthen the back of your neck.",
            "Squeeze your shoulder blades together for 5 seconds, release.",
            "Repeat 5 times, breathing slowly."
        )
    ),
    WellnessMove(
        "Eye rest (20-20-20)", "Relieve screen strain", Icons.Filled.RemoveRedEye,
        listOf(
            "Every 20 minutes, look at something 20 feet away.",
            "Hold your gaze for at least 20 seconds.",
            "Blink fully a few times to refresh your eyes."
        )
    ),
    WellnessMove(
        "Hydration check", "Fuel performance", Icons.Filled.WaterDrop,
        listOf(
            "Aim for a glass of water now.",
            "Keep a bottle within reach and sip through the day.",
            "Target roughly 35 ml per kg of body weight daily."
        )
    ),
    WellnessMove(
        "Movement snack", "Wake up your body", Icons.AutoMirrored.Filled.DirectionsWalk,
        listOf(
            "Stand up and walk for 2–3 minutes.",
            "Add 10 bodyweight squats and 10 arm circles.",
            "Finish with a big inhale and a slow exhale."
        )
    )
)

@Composable
private fun DietCard(onOpen: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(AccentGreen, AccentBlue))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.RestaurantMenu, null, tint = Ink, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Diet plan", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Meals for your target · veg or non-veg · build from what you have", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun BreathingCard(onOpen: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Air, null, tint = Ink, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Guided breathing", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Box, 4-7-8 and calm patterns to relax & recover", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun WellnessRow(move: WellnessMove, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), cornerRadius = 16.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(move.icon, null, tint = AccentViolet, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(move.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(move.subtitle, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun DailyTipCard() {
    val tip = tipOfTheDay()
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(AccentGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.TipsAndUpdates, null, tint = Ink, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "TIP OF THE DAY",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentAmber,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(tip, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
        }
    }
}

private val dailyTips = listOf(
    "Progressive overload is king — add a little weight, a rep, or a set over time to keep growing.",
    "Protein fuels muscle repair. Aim for a palm-sized portion at every meal.",
    "Warm up first. 5 minutes of light movement primes your joints and cuts injury risk.",
    "Sleep is when you actually build muscle. Target 7–9 hours a night.",
    "Form over ego. Controlled reps with good technique beat sloppy heavy lifts.",
    "Stay hydrated — even mild dehydration saps strength and focus. Sip throughout the day.",
    "Rest days are growth days. Recovery lets your body adapt and get stronger.",
    "Full range of motion builds more muscle than half-reps with heavier weight.",
    "Consistency beats intensity. Showing up 4x a week for months wins every time.",
    "Breathe out on exertion, in on the way down — never hold your breath under load.",
    "Don't skip legs. Big lower-body lifts drive whole-body strength and conditioning.",
    "Track your workouts. What gets measured gets improved.",
    "Stretch after training while muscles are warm to keep mobility in check.",
    "A small calorie surplus builds muscle; a small deficit burns fat. Pick a goal and be patient.",
)

private fun tipOfTheDay(): String =
    dailyTips[(LocalDate.now().dayOfYear - 1).mod(dailyTips.size)]

@Composable
private fun TodayCard(
    state: HomeUiState,
    onOpenDay: (Int) -> Unit,
    onStartSession: (Int) -> Unit,
    onRename: () -> Unit
) {
    val today = state.today
    val displayTitle = today?.title?.takeIf { it.isNotBlank() }
        ?: state.suggestedName.takeIf { it.isNotBlank() }
        ?: "Rest"
    val canRename = today != null && !today.isRestDay
    GlassCard(Modifier.fillMaxWidth(), onClick = { onOpenDay(state.todayDow) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("TODAY", style = MaterialTheme.typography.labelMedium, color = AccentBlue, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        displayTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (canRename) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Rename today",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onRename() }
                        )
                    }
                }
                Text(
                    if (today?.isRestDay == true) "Recovery day" else today?.focus ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (today != null && !today.isRestDay && today.scheduledMinutes >= 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Scheduled ${minutesToTimeLabel(today.scheduledMinutes)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentViolet
                    )
                }
            }
            ProgressRing(
                progress = if (state.completedToday) 1f else if (state.total == 0) 0f else 0.05f,
                size = 78.dp
            ) {
                if (state.completedToday) {
                    Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(28.dp))
                } else {
                    Text("${state.total}", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.total > 0) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("${state.warmups}", "warm-ups")
                MiniStat("${state.mains}", "exercises")
                MiniStat("${state.stretches}", "stretches")
            }
            Spacer(Modifier.height(12.dp))
            GradientButton(
                text = if (state.completedToday) "Train again" else "Start Today's Workout",
                modifier = Modifier.fillMaxWidth(),
                onClick = { onStartSession(state.todayDow) }
            )
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = AccentBlue, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
private fun WeekDot(
    day: WorkoutDayEntity,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isToday) Surface2.copy(alpha = 0.8f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            dayShort(day.dayOfWeek).take(1),
            style = MaterialTheme.typography.labelMedium,
            color = if (isToday) TextPrimary else TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (day.isRestDay) Brush.linearGradient(listOf(Surface2, Surface2))
                    else Brush.linearGradient(listOf(AccentBlue, AccentViolet))
                )
        )
    }
}

@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier) {
        icon()
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

private fun greeting(): String {
    val h = LocalTime.now().hour
    return when {
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
