package com.buildbygod.ui.session

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.notifications.WorkoutSessionService
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.components.DemoMedia
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.theme.AccentBlue
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
    val context = LocalContext.current

    LaunchedEffect(state.finished) {
        if (state.finished) {
            WorkoutSessionService.stop(context)
            onFinish()
        }
    }

    // Auto-pause when the app is backgrounded; resume when it comes forward again. The session keeps
    // running in the persisted store and the notification stays visible the whole time.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> if (!vm.state.value.finished) vm.setPaused(true)
                Lifecycle.Event.ON_START -> if (!vm.state.value.finished) vm.setPaused(false)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Keep an ongoing notification (title, current exercise, paused/live timer) while training, so it
    // stays visible after the user backgrounds the app. Stopped only when the workout is finished.
    LaunchedEffect(state.current?.exercise?.name, state.index, state.items.size, state.title, state.paused, state.finished) {
        val current = state.current
        if (!state.finished && current != null && state.items.isNotEmpty()) {
            WorkoutSessionService.start(
                context = context,
                title = state.title,
                exercise = current.exercise.name,
                position = state.index + 1,
                total = state.items.size,
                startedAt = vm.startedAt,
                paused = state.paused
            )
        }
    }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar(title = state.title, onBack = { vm.setPaused(true); onFinish() })

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${type.label}  -  ${state.index + 1} / ${state.items.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = muscle.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        current.exercise.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface2)
                        .clickable { vm.setPaused(!state.paused) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (state.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (state.paused) "Resume" else "Pause",
                        tint = AccentGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            if (state.paused) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("Paused", color = AccentBlue, fontWeight = FontWeight.Bold)
                    Text("Your progress is saved. Resume any time from here, Home or the notification.", color = TextSecondary)
                }
            }

            DemoMedia(
                clipAsset = current.exercise.clipAsset,
                accent = muscle.accent,
                label = current.exercise.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(22.dp))
            )

            if (current.effectiveDuration > 0) {
                TimerCard(seconds = current.effectiveDuration, key = current.dxId)
            } else {
                SetTrackerCard(totalSets = current.dxSets, reps = current.dxReps, key = current.dxId)
            }

            HowToCard(instructions = current.exercise.instructions, tips = current.exercise.tips)

            Spacer(Modifier.height(4.dp))

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
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HowToCard(instructions: String, tips: String) {
    var expanded by remember { mutableStateOf(true) }
    val steps = instructions.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    if (steps.isEmpty() && tips.isBlank()) return

    GlassCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("How to do it", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(if (expanded) "Hide" else "Show", style = MaterialTheme.typography.labelMedium, color = AccentBlue)
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEachIndexed { i, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(22.dp).clip(RoundedCornerShape(50)).background(AccentBlue.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = AccentBlue)
                        }
                        Text(step, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
                    }
                }
                if (tips.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentGreen.copy(alpha = 0.12f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                        Text(tips, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }
    }
}

/** Tracks completed sets with a rest countdown between them, for rep-based exercises. */
@Composable
private fun SetTrackerCard(totalSets: Int, reps: String, key: Long) {
    val sets = totalSets.coerceAtLeast(1)
    var doneSets by remember(key) { mutableIntStateOf(0) }
    var resting by remember(key) { mutableStateOf(false) }
    var rest by remember(key) { mutableIntStateOf(0) }

    LaunchedEffect(key, resting) {
        if (resting) {
            while (rest > 0) {
                delay(1000)
                rest -= 1
            }
            resting = false
        }
    }

    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Target", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("$sets sets  x  $reps", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Text("$doneSets / $sets", style = MaterialTheme.typography.headlineSmall, color = AccentGreen, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(sets) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (i < doneSets) AccentGreen else Surface2)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (resting) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgressRing(progress = 0f, size = 56.dp) {
                    Text("${rest}s", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Rest", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Next set coming up", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(Surface2).clickable { resting = false; rest = 0 }.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Skip rest", color = AccentBlue, style = MaterialTheme.typography.labelMedium)
                }
            }
        } else if (doneSets < sets) {
            GradientButton(
                text = if (doneSets == 0) "Start set 1" else "Done set ${doneSets} - rest",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    doneSets += 1
                    if (doneSets < sets) {
                        rest = 45
                        resting = true
                    }
                }
            )
        } else {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AccentGreen.copy(alpha = 0.15f)).padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("All sets done - hit Done below", color = AccentGreen, fontWeight = FontWeight.SemiBold)
            }
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
