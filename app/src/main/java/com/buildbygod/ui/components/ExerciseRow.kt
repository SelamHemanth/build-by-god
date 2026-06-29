package com.buildbygod.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.domain.model.Difficulty
import com.buildbygod.domain.model.Equipment
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

@Composable
fun ExerciseRow(
    exercise: ExerciseEntity,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onToggleFavorite: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit
) {
    val muscle = MuscleGroup.fromName(exercise.muscleGroup)
    val equipment = Equipment.fromName(exercise.equipment)
    val difficulty = Difficulty.fromName(exercise.difficulty)
    GlassCard(
        modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExerciseThumb(
                clipAsset = exercise.clipAsset,
                accent = muscle.accent,
                contentDescription = exercise.name,
                size = 46.dp
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(muscle.label, style = MaterialTheme.typography.labelMedium, color = muscle.accent)
                    Text("-", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(equipment.label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text("-", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(difficulty.label, style = MaterialTheme.typography.labelMedium, color = difficulty.accent, fontWeight = FontWeight.SemiBold)
                }
            }
            if (trailing != null) {
                trailing()
            } else if (onToggleFavorite != null) {
                IconButton(onClick = { onToggleFavorite(!exercise.isFavorite) }) {
                    Icon(
                        if (exercise.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (exercise.isFavorite) AccentAmber else TextSecondary
                    )
                }
            }
        }
    }
}
