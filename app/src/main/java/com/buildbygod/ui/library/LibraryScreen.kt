package com.buildbygod.ui.library

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.Difficulty
import com.buildbygod.domain.model.Equipment
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.components.ControlsRow
import com.buildbygod.ui.components.ExerciseRow
import com.buildbygod.ui.components.FilterChip
import com.buildbygod.ui.components.ListSearchField
import com.buildbygod.ui.components.SortChip
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    onOpenExercise: (String) -> Unit,
    vm: LibraryViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    // Back should return to the muscle-group grid instead of dropping out to Home.
    BackHandler(enabled = !state.showGroups) { vm.backToGroups() }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Exercise Library",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (state.showGroups) "Pick a muscle group to explore"
                else "Browse moves by muscle target",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            ListSearchField(
                query = state.query,
                onQuery = vm::onQuery,
                placeholder = "Search all exercises",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            ControlsRow {
                SortChip(
                    options = ExerciseSort.entries,
                    selected = state.sort,
                    label = { it.label },
                    onSelect = vm::onSort
                )
                Difficulty.entries.forEach { d ->
                    FilterChip(
                        label = d.label,
                        selected = state.selectedDifficulty == d,
                        accent = d.accent,
                        onClick = { vm.onDifficulty(d) }
                    )
                }
                Equipment.entries.forEach { e ->
                    FilterChip(
                        label = e.label,
                        selected = state.selectedEquipment == e,
                        onClick = { vm.onEquipment(e) }
                    )
                }
            }
            if (!state.showGroups) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "${state.resultCount} exercise${if (state.resultCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        if (state.showGroups) {
            // ---- Level 1: muscle-group icon grid ----
            item {
                GroupTile(
                    label = "Favorites",
                    count = null,
                    accent = AccentAmber,
                    icon = Icons.Filled.FavoriteBorder,
                    modifier = Modifier.fillMaxWidth()
                ) { vm.toggleFavoritesOnly() }
                Spacer(Modifier.height(10.dp))
            }
            items(MuscleGroup.entries.chunked(2)) { rowGroups ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowGroups.forEach { g ->
                        GroupTile(
                            label = g.label,
                            count = state.groupCounts[g] ?: 0,
                            accent = g.accent,
                            icon = iconFor(g),
                            modifier = Modifier.weight(1f)
                        ) { vm.onMuscle(g) }
                    }
                    if (rowGroups.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }
        } else {
            // ---- Level 2: exercises for the selection ----
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { vm.backToGroups() }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AccentBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "All groups",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            val isEmpty = if (state.showByDifficulty) state.byDifficulty.isEmpty() else state.grouped.isEmpty()
            if (isEmpty) {
                item {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        if (state.favoritesOnly) "No favorites yet — tap the star on any exercise."
                        else "No exercises match your search.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }

            if (state.showByDifficulty) {
                // ---- Single group: organise by Beginner / Intermediate / Advanced ----
                state.byDifficulty.forEach { (difficulty, list) ->
                    item(key = "d_${difficulty.name}") {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(difficulty.accent.copy(alpha = 0.18f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    difficulty.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = difficulty.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${list.size}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                    items(list, key = { it.id }) { ex ->
                        ExerciseRow(
                            exercise = ex,
                            onToggleFavorite = { vm.toggleFavorite(ex.id, it) },
                            onClick = { onOpenExercise(ex.id) }
                        )
                    }
                }
            } else {
                state.grouped.forEach { (muscle, list) ->
                    item(key = "h_${muscle.name}") {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                muscle.label,
                                style = MaterialTheme.typography.titleLarge,
                                color = muscle.accent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${list.size}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                    items(list, key = { it.id }) { ex ->
                        ExerciseRow(
                            exercise = ex,
                            onToggleFavorite = { vm.toggleFavorite(ex.id, it) },
                            onClick = { onOpenExercise(ex.id) }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(90.dp)) }
    }
}

@Composable
private fun GroupTile(
    label: String,
    count: Int?,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.45f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (count == null) "Your saved moves" else "$count exercises",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

/** Every group currently uses the dumbbell glyph, tinted by its accent color. */
private fun iconFor(group: MuscleGroup): ImageVector = when (group) {
    else -> Icons.Filled.FitnessCenter
}
