package com.buildbygod.ui.exercise

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.buildbygod.domain.model.Equipment
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.components.DemoMedia
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentGradient
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

@Composable
fun ExerciseDetailScreen(
    onBack: () -> Unit,
    vm: ExerciseDetailViewModel = hiltViewModel()
) {
    val exercise by vm.exercise.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        val ex = exercise
        GlassTopBar(
            title = ex?.name ?: "Exercise",
            onBack = onBack,
            actions = {
                if (ex != null) {
                    IconButton(onClick = { vm.toggleFavorite(!ex.isFavorite) }) {
                        Icon(
                            if (ex.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (ex.isFavorite) AccentAmber else TextSecondary
                        )
                    }
                }
            }
        )

        if (ex == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Loading...", color = TextSecondary)
            }
            return@Column
        }

        val muscle = MuscleGroup.fromName(ex.muscleGroup)
        val equipment = Equipment.fromName(ex.equipment)
        val type = runCatching { ExerciseType.valueOf(ex.type) }.getOrDefault(ExerciseType.MAIN)
        val meta = ExerciseGuide.parse(ex.tips)
        val overview = ExerciseGuide.overview(ex.name, muscle, equipment, type, meta)
        val coachTips = ExerciseGuide.tips(muscle, type, meta)
        val steps = ex.instructions.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DemoMedia(
                clipAsset = ex.clipAsset,
                accent = muscle.accent,
                label = ex.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(22.dp))
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(label = "Target", value = muscle.label)
                Stat(label = "Equipment", value = equipment.label)
                Stat(
                    label = if (ex.durationSeconds > 0) "Duration" else "Sets x Reps",
                    value = if (ex.durationSeconds > 0) "${ex.durationSeconds}s" else "${ex.defaultSets} x ${ex.defaultReps}"
                )
            }

            // ---- Muscles worked ----
            GlassCard(Modifier.fillMaxWidth()) {
                Text("Muscles worked", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "Primary: ${muscle.label}",
                    style = MaterialTheme.typography.labelLarge,
                    color = muscle.accent,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                MuscleMap(group = muscle, accent = muscle.accent, modifier = Modifier.fillMaxWidth())
                if (meta.tags.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        meta.tags.forEach { TagChip(it) }
                    }
                }
            }

            // ---- Overview ----
            GlassCard(Modifier.fillMaxWidth()) {
                Text("Overview", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(overview, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            // ---- Step by step ----
            GlassCard(Modifier.fillMaxWidth()) {
                Text("How to do it", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Step by step — take your time on each one.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                steps.forEachIndexed { i, step ->
                    Row(Modifier.padding(vertical = 6.dp)) {
                        Box(
                            Modifier.size(26.dp).clip(CircleShape).background(AccentGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${i + 1}", color = Ink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                        )
                    }
                }
            }

            // ---- Tips ----
            GlassCard(Modifier.fillMaxWidth()) {
                Text("Tips for good form", style = MaterialTheme.typography.titleMedium, color = AccentAmber, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                coachTips.forEach { tip ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Text(
                            tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }

            Text(
                "Section: ${type.label}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Surface2.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Stat(label: String, value: String) {
    GlassCard(Modifier.weight(1f), cornerRadius = 18.dp) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}
