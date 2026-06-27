package com.buildbygod.ui.progress

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.CheckCircle
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
import com.buildbygod.data.local.entity.SessionLogEntity
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.util.dayFull
import java.time.LocalDate

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

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat(Icons.Filled.LocalFireDepartment, AccentAmber, "${state.streak}", "Streak", Modifier.weight(1f))
                Stat(Icons.Filled.CalendarMonth, AccentBlue, "${state.thisWeek}", "This week", Modifier.weight(1f))
                Stat(Icons.Filled.CheckCircle, AccentGreen, "${state.total}", "Total", Modifier.weight(1f))
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text("Last 8 weeks", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Heatmap(active = state.activeDays)
            }
        }

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

        items(state.sessions, key = { it.id }) { s ->
            SessionRow(s)
        }

        item { Spacer(Modifier.height(90.dp)) }
    }
}

@Composable
private fun Heatmap(active: Set<Long>) {
    val today = LocalDate.now().toEpochDay()
    val weeks = 8
    val start = today - (weeks * 7 - 1)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until 7) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until weeks) {
                    val epoch = start + col * 7 + row
                    val isActive = active.contains(epoch)
                    val future = epoch > today
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isActive -> Brush.linearGradient(listOf(AccentBlue, AccentViolet))
                                    future -> Brush.linearGradient(listOf(Surface2.copy(alpha = 0.3f), Surface2.copy(alpha = 0.3f)))
                                    else -> Brush.linearGradient(listOf(Surface2, Surface2))
                                }
                            )
                    )
                }
            }
        }
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
    tint: androidx.compose.ui.graphics.Color,
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
