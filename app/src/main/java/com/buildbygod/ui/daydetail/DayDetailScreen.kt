package com.buildbygod.ui.daydetail

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.local.dao.DayExerciseWithInfo
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGradient
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GradientButton
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.InkElevated
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.util.dayFull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    day: Int,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onStartSession: () -> Unit,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var addSection by remember { mutableStateOf<ExerciseType?>(null) }

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        GlassTopBar(title = state.day?.title ?: dayFull(day), onBack = onBack)

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(dayFull(day), style = MaterialTheme.typography.labelLarge, color = AccentBlue)
                state.day?.let {
                    Text(it.focus, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
            }

            if (state.day?.isRestDay == true && state.total == 0) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text("Rest Day", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Recover well. Light mobility and stretching are added below if you want to move.", color = TextSecondary)
                    }
                }
            }

            section("Warm-up", ExerciseType.WARMUP, state.warmups, onOpenExercise, vm::remove) { addSection = it }
            section("Exercises", ExerciseType.MAIN, state.mains, onOpenExercise, vm::remove) { addSection = it }
            section("Stretches", ExerciseType.STRETCH, state.stretches, onOpenExercise, vm::remove) { addSection = it }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (state.total > 0) {
        Box(Modifier.fillMaxSize().padding(bottom = 8.dp), contentAlignment = Alignment.BottomCenter) {
            GradientButton(
                text = "Start Workout  (${state.total} moves)",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = onStartSession
            )
        }
    }
    }

    val sheetState = rememberModalBottomSheetState()
    val section = addSection
    if (section != null) {
        val options by vm.pickerFor(section).collectAsState(initial = emptyList())
        ModalBottomSheet(
            onDismissRequest = { addSection = null },
            sheetState = sheetState,
            containerColor = InkElevated
        ) {
            Text(
                "Add ${section.label}",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyColumn(
                Modifier
                    .heightIn(max = 460.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(options, key = { it.id }) { ex ->
                    val muscle = MuscleGroup.fromName(ex.muscleGroup)
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        onClick = { vm.addExercise(ex, section); addSection = null }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(muscle.accent)
                            )
                            Text(
                                ex.name,
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f).padding(start = 12.dp)
                            )
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = AccentBlue)
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    type: ExerciseType,
    rows: List<DayExerciseWithInfo>,
    onOpenExercise: (String) -> Unit,
    onRemove: (Long) -> Unit,
    onAdd: (ExerciseType) -> Unit
) {
    item(key = "header_$title") {
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Surface2.copy(alpha = 0.7f))
                    .clickable { onAdd(type) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Text("Add", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }
    }
    if (rows.isEmpty()) {
        item(key = "empty_$title") {
            Text("Nothing here yet.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
    items(rows, key = { "${title}_${it.dxId}" }) { item ->
        DayExerciseRow(item, onClick = { onOpenExercise(item.exercise.id) }, onRemove = { onRemove(item.dxId) })
    }
}

@Composable
private fun DayExerciseRow(
    item: DayExerciseWithInfo,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val muscle = MuscleGroup.fromName(item.exercise.muscleGroup)
    GlassCard(Modifier.fillMaxWidth(), cornerRadius = 18.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(muscle.accent, muscle.accent.copy(alpha = 0.4f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.exercise.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    if (item.exercise.durationSeconds > 0) "${item.exercise.durationSeconds}s"
                    else "${item.dxSets} sets x ${item.dxReps}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = TextSecondary)
            }
        }
    }
}
