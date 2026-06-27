package com.buildbygod.ui.exercise

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

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
                    .clip(RoundedCornerShape(22.dp)),
                onPlayExternal = {
                    ex.youtubeUrl?.let {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                    }
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(label = "Target", value = muscle.label)
                Stat(label = "Equipment", value = equipment.label)
                Stat(
                    label = if (ex.durationSeconds > 0) "Duration" else "Sets x Reps",
                    value = if (ex.durationSeconds > 0) "${ex.durationSeconds}s" else "${ex.defaultSets} x ${ex.defaultReps}"
                )
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Text("How to do it", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                ex.instructions.split("\n").filter { it.isNotBlank() }.forEachIndexed { i, step ->
                    Row(Modifier.padding(vertical = 5.dp)) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape).background(AccentGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${i + 1}", color = Ink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            if (ex.tips.isNotBlank()) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("Pro tip", style = MaterialTheme.typography.titleMedium, color = AccentAmber, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(ex.tips, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            if (ex.youtubeUrl != null) {
                GlassCard(
                    Modifier.fillMaxWidth(),
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ex.youtubeUrl))) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = muscle.accent, modifier = Modifier.size(28.dp))
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Watch full video", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Open a demo on YouTube", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
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
private fun androidx.compose.foundation.layout.RowScope.Stat(label: String, value: String) {
    GlassCard(Modifier.weight(1f), cornerRadius = 18.dp) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}
