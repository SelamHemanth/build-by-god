package com.buildbygod.ui.session

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.components.DemoMedia
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GradientButton
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.ProgressRing
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SessionScreen(
    day: Int,
    onFinish: () -> Unit,
    vm: SessionViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinish()
    }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar(title = state.title, onBack = onFinish)

        val animatedProgress by animateFloatAsState(state.progress, label = "p")
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Surface2)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AccentGreen)
            )
        }

        val current = state.current
        if (state.loading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Loading...", color = TextSecondary) }
            return@Column
        }
        if (current == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No exercises in this workout.", color = TextSecondary)
            }
            return@Column
        }

        val muscle = MuscleGroup.fromName(current.exercise.muscleGroup)
        val type = runCatching { ExerciseType.valueOf(current.dxSection) }.getOrDefault(ExerciseType.MAIN)

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "${type.label}  -  ${state.index + 1} / ${state.items.size}",
                style = MaterialTheme.typography.labelLarge,
                color = muscle.accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                current.exercise.name,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            DemoMedia(
                clipAsset = current.exercise.clipAsset,
                accent = muscle.accent,
                label = current.exercise.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(22.dp))
            )

            if (current.exercise.durationSeconds > 0) {
                TimerCard(seconds = current.exercise.durationSeconds, key = current.dxId)
            } else {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("Target", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        "${current.dxSets} sets  x  ${current.dxReps}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.index > 0) {
                    GlassCard(Modifier.weight(1f), onClick = vm::goPrevious) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Back", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (!state.isLast) {
                    GlassCard(Modifier.weight(1f), onClick = vm::skipNext) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Skip", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            GradientButton(
                text = if (state.isLast) "Finish Workout" else "Done - Next",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (state.isLast) {
                        vm.markDoneAndNext()
                        vm.finish()
                    } else {
                        vm.markDoneAndNext()
                    }
                }
            )
        }
    }
}

@Composable
private fun TimerCard(seconds: Int, key: Long) {
    var remaining by remember(key) { mutableIntStateOf(seconds) }
    var running by remember(key) { mutableStateOf(true) }

    LaunchedEffect(key, running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        if (remaining == 0) running = false
    }

    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressRing(
                progress = if (seconds == 0) 0f else (seconds - remaining).toFloat() / seconds,
                size = 96.dp
            ) {
                Text(
                    "${remaining}s",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.weight(1f)) {
                Text("Hold / perform", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    if (running) "Timer running" else if (remaining == 0) "Done!" else "Paused",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface2)
                    .clickable { if (remaining > 0) running = !running }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Toggle timer",
                    tint = AccentGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
