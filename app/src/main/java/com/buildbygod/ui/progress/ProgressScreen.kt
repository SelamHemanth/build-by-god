package com.buildbygod.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.local.entity.SessionLogEntity
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.Pill
import com.buildbygod.ui.theme.ProgressRing
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.util.dayFull
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgressScreen(vm: ProgressViewModel = hiltViewModel()) {
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
            Text("Progress", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Consistency builds results", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        // Range selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ProgressRange.entries) { r ->
                    Pill(text = r.label, selected = state.range == r, onClick = { vm.setRange(r) })
                }
            }
        }

        // Range-scaled stats
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat(Icons.Filled.CheckCircle, AccentGreen, "${state.rangeWorkouts}", "Workouts", Modifier.weight(1f))
                Stat(Icons.Filled.Timer, AccentBlue, formatMinutes(state.rangeMinutes), "Trained", Modifier.weight(1f))
                Stat(Icons.Filled.LocalFireDepartment, AccentAmber, "${state.streak}", "Streak", Modifier.weight(1f))
            }
        }

        // Consistency ring for the chosen window
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(progress = state.consistency, size = 86.dp) {
                        Text(
                            "${(state.consistency * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Consistency", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "Trained ${state.rangeActiveDays} of ${state.range.days} days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            "${state.total} workouts all-time",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentViolet
                        )
                    }
                }
            }
        }

        // Calendar
        item { CalendarCard(state, onPrev = vm::previousMonth, onNext = vm::nextMonth) }

        item {
            Text("Recent sessions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }

        if (state.sessions.isEmpty()) {
            item {
                Text(
                    "No workouts logged yet. Finish a session to see it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        items(state.sessions, key = { it.id }) { s -> SessionRow(s) }

        item { Spacer(Modifier.height(90.dp)) }
    }
}

@Composable
private fun CalendarCard(state: ProgressUiState, onPrev: () -> Unit, onNext: () -> Unit) {
    val month = state.month
    val today = LocalDate.now()
    val isCurrentMonth = month == java.time.YearMonth.now()

    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month", onClick = onPrev)
            Text(
                "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            RoundIconButton(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month",
                enabled = !isCurrentMonth, onClick = onNext
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Text(
                    d,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        val firstDow = month.atDay(1).dayOfWeek.value // Mon=1..Sun=7
        val leading = firstDow - 1
        val length = month.lengthOfMonth()
        val cells = leading + length
        val rows = (cells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (col in 0 until 7) {
                        val cell = row * 7 + col
                        val dayNum = cell - leading + 1
                        if (dayNum in 1..length) {
                            val date = month.atDay(dayNum)
                            val active = state.activeDays.contains(date.toEpochDay())
                            val isToday = date == today
                            val isFuture = date.isAfter(today)
                            DayCell(dayNum, active, isToday, isFuture, Modifier.weight(1f))
                        } else {
                            Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, active: Boolean, isToday: Boolean, isFuture: Boolean, modifier: Modifier) {
    Box(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    active -> Brush.linearGradient(listOf(AccentBlue, AccentViolet))
                    isToday -> Brush.linearGradient(listOf(Surface2, Surface2))
                    else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$day",
            style = MaterialTheme.typography.labelMedium,
            color = when {
                active -> Ink
                isFuture -> TextSecondary.copy(alpha = 0.4f)
                isToday -> AccentBlue
                else -> TextSecondary
            },
            fontWeight = if (active || isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Surface2.copy(alpha = if (enabled) 0.7f else 0.25f))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = desc,
            tint = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SessionRow(s: SessionLogEntity) {
    GlassCard(Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "${dayFull(s.dayOfWeek)}  -  ${s.completedCount}/${s.totalCount} done  -  ${s.durationSeconds / 60} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            val pct = if (s.totalCount == 0) 0 else (s.completedCount * 100 / s.totalCount)
            Text("$pct%", style = MaterialTheme.typography.titleMedium, color = AccentGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Stat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

private fun formatMinutes(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"
