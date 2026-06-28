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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGradient
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GradientButton
import com.buildbygod.ui.theme.Ink
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
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(greeting(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text(
                state.name.ifBlank { "Athlete" },
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        item { TodayCard(state, onOpenDay, onStartSession) }

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
            DailyTipCard()
            Spacer(Modifier.height(90.dp))
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
    onStartSession: (Int) -> Unit
) {
    val today = state.today
    GlassCard(Modifier.fillMaxWidth(), onClick = { onOpenDay(state.todayDow) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("TODAY", style = MaterialTheme.typography.labelMedium, color = AccentBlue, fontWeight = FontWeight.Bold)
                Text(
                    today?.title ?: "Rest",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
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
