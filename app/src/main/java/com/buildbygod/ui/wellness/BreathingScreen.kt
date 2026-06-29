package com.buildbygod.ui.wellness

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.Pill
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

/** A breathing phase: a label plus how long it lasts (seconds) and the target scale of the orb. */
private data class Phase(val label: String, val seconds: Int, val targetScale: Float)

private enum class BreathPattern(val label: String, val phases: List<Phase>) {
    BOX(
        "Box 4-4-4-4",
        listOf(
            Phase("Breathe in", 4, 1f),
            Phase("Hold", 4, 1f),
            Phase("Breathe out", 4, 0.55f),
            Phase("Hold", 4, 0.55f)
        )
    ),
    RELAX(
        "Relax 4-7-8",
        listOf(
            Phase("Breathe in", 4, 1f),
            Phase("Hold", 7, 1f),
            Phase("Breathe out", 8, 0.55f)
        )
    ),
    CALM(
        "Calm 5-5",
        listOf(
            Phase("Breathe in", 5, 1f),
            Phase("Breathe out", 5, 0.55f)
        )
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun BreathingScreen(onBack: () -> Unit) {
    val tokens = LocalFitTokens.current
    var pattern by remember { mutableStateOf(BreathPattern.BOX) }
    var running by remember { mutableStateOf(false) }
    var phaseIndex by remember(pattern) { mutableIntStateOf(0) }
    var secondsLeft by remember(pattern) { mutableIntStateOf(pattern.phases[0].seconds) }
    var cycles by remember(pattern) { mutableIntStateOf(0) }

    val phase = pattern.phases[phaseIndex]

    // Drive the phase clock while running.
    LaunchedEffect(running, pattern) {
        if (!running) return@LaunchedEffect
        while (running) {
            delay(1000)
            if (secondsLeft > 1) {
                secondsLeft -= 1
            } else {
                val next = (phaseIndex + 1) % pattern.phases.size
                if (next == 0) cycles += 1
                phaseIndex = next
                secondsLeft = pattern.phases[next].seconds
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (running) phase.targetScale else 0.7f,
        animationSpec = tween(durationMillis = if (running) phase.seconds * 1000 else 400, easing = LinearEasing),
        label = "breath"
    )

    Column(Modifier.fillMaxSize()) {
        GlassTopBar(title = "Breathing", onBack = onBack)

        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BreathPattern.entries.forEach { p ->
                    Pill(p.label, pattern == p, onClick = { pattern = p; running = false; phaseIndex = 0; secondsLeft = p.phases[0].seconds; cycles = 0 })
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(260.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(tokens.accent.copy(alpha = 0.85f), tokens.accent.copy(alpha = 0.15f))))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (running) phase.label else "Ready",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (running) {
                        Text("$secondsLeft", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                "Cycles completed: $cycles",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(Modifier.weight(1f))

            GlassCard(Modifier.fillMaxWidth(), onClick = {
                if (running) {
                    running = false
                } else {
                    phaseIndex = 0; secondsLeft = pattern.phases[0].seconds; running = true
                }
            }) {
                Text(
                    if (running) "Pause" else "Start",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.accent,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
