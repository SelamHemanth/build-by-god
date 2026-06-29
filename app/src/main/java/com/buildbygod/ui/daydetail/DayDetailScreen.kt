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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.local.dao.DayExerciseWithInfo
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.domain.model.Difficulty
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.components.ControlsRow
import com.buildbygod.ui.components.ExerciseThumb
import com.buildbygod.ui.components.FilterChip
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.components.ListSearchField
import com.buildbygod.ui.components.SortChip
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGradient
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GradientButton
import com.buildbygod.ui.theme.InkElevated
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.util.dayFull

/** Ordering options for the add-exercise picker. */
private enum class PickerSort(val label: String) {
    SUGGESTED("Suggested"),
    NAME_AZ("Name A–Z"),
    EASIEST("Easiest first"),
    HARDEST("Hardest first")
}

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
    // Pending new exercise awaiting sets/reps/time customization before it's added.
    var customizeNew by remember { mutableStateOf<Pair<ExerciseEntity, ExerciseType>?>(null) }
    // Existing day-exercise being edited.
    var editing by remember { mutableStateOf<DayExerciseWithInfo?>(null) }

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

            section("Warm-up", ExerciseType.WARMUP, state.warmups, onOpenExercise, vm::remove, { editing = it }) { addSection = it }
            section("Exercises", ExerciseType.MAIN, state.mains, onOpenExercise, vm::remove, { editing = it }) { addSection = it }
            section("Stretches", ExerciseType.STRETCH, state.stretches, onOpenExercise, vm::remove, { editing = it }) { addSection = it }

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
        val options by vm.suggestedPickerFor(section).collectAsState(initial = emptyList())
        var pickerQuery by remember(section) { mutableStateOf("") }
        var pickerDifficulty by remember(section) { mutableStateOf<Difficulty?>(null) }
        var pickerSort by remember(section) { mutableStateOf(PickerSort.SUGGESTED) }

        val visibleOptions = remember(options, pickerQuery, pickerDifficulty, pickerSort) {
            options
                .filter { pickerQuery.isBlank() || it.exercise.name.contains(pickerQuery, ignoreCase = true) }
                .filter { pickerDifficulty == null || it.exercise.difficulty == pickerDifficulty!!.name }
                .let { list ->
                    when (pickerSort) {
                        PickerSort.SUGGESTED -> list
                        PickerSort.NAME_AZ -> list.sortedBy { it.exercise.name.lowercase() }
                        PickerSort.EASIEST -> list.sortedBy { Difficulty.fromName(it.exercise.difficulty).rank }
                        PickerSort.HARDEST -> list.sortedByDescending { Difficulty.fromName(it.exercise.difficulty).rank }
                    }
                }
        }

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
            Text(
                "Picks for your level are marked Suggested.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            ListSearchField(
                query = pickerQuery,
                onQuery = { pickerQuery = it },
                placeholder = "Search ${section.label.lowercase()}s",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            ControlsRow(Modifier.padding(horizontal = 16.dp)) {
                SortChip(
                    options = PickerSort.entries,
                    selected = pickerSort,
                    label = { it.label },
                    onSelect = { pickerSort = it }
                )
                Difficulty.entries.forEach { d ->
                    FilterChip(
                        label = d.label,
                        selected = pickerDifficulty == d,
                        accent = d.accent,
                        onClick = { pickerDifficulty = if (pickerDifficulty == d) null else d }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                Modifier
                    .heightIn(max = 460.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (visibleOptions.isEmpty()) {
                    item {
                        Text(
                            "No matches. Try a different search or filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                items(visibleOptions, key = { it.exercise.id }) { picker ->
                    val ex = picker.exercise
                    val muscle = MuscleGroup.fromName(ex.muscleGroup)
                    val difficulty = Difficulty.fromName(ex.difficulty)
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        onClick = { customizeNew = ex to section; addSection = null }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ExerciseThumb(
                                clipAsset = ex.clipAsset,
                                accent = muscle.accent,
                                contentDescription = ex.name,
                                size = 40.dp,
                                cornerRadius = 12.dp
                            )
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(
                                    ex.name,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Pill(difficulty.label, difficulty.accent)
                                    if (picker.suggested) Pill("Suggested", AccentBlue)
                                }
                            }
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = AccentBlue)
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    customizeNew?.let { (ex, sec) ->
        PrescriptionDialog(
            title = "Add ${ex.name}",
            initialSets = ex.defaultSets,
            initialReps = ex.defaultReps,
            initialDuration = ex.durationSeconds,
            timed = ex.durationSeconds > 0,
            onDismiss = { customizeNew = null },
            onConfirm = { sets, reps, duration ->
                vm.addExercise(ex, sec, sets, reps, duration)
                customizeNew = null
            }
        )
    }

    editing?.let { item ->
        PrescriptionDialog(
            title = "Edit ${item.exercise.name}",
            initialSets = item.dxSets,
            initialReps = item.dxReps,
            initialDuration = item.effectiveDuration,
            timed = item.exercise.durationSeconds > 0,
            onDismiss = { editing = null },
            onConfirm = { sets, reps, duration ->
                vm.updatePrescription(item.dxId, sets, reps, duration)
                editing = null
            }
        )
    }
}

@Composable
private fun Pill(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun PrescriptionDialog(
    title: String,
    initialSets: Int,
    initialReps: String,
    initialDuration: Int,
    timed: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (sets: Int, reps: String, duration: Int) -> Unit
) {
    var sets by remember { mutableStateOf(initialSets.coerceAtLeast(1).toString()) }
    var reps by remember { mutableStateOf(initialReps) }
    var duration by remember { mutableStateOf(if (initialDuration > 0) initialDuration.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val s = sets.toIntOrNull()?.coerceIn(1, 20) ?: initialSets.coerceAtLeast(1)
                val d = if (timed) (duration.toIntOrNull()?.coerceIn(5, 1800) ?: -1) else -1
                onConfirm(s, reps.ifBlank { initialReps }, d)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(title, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it.filter(Char::isDigit).take(2) },
                    label = { Text("Sets") },
                    singleLine = true
                )
                if (timed) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it.filter(Char::isDigit).take(4) },
                        label = { Text("Time per set (seconds)") },
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps (e.g. 8-12)") },
                        singleLine = true
                    )
                }
            }
        },
        containerColor = InkElevated
    )
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    type: ExerciseType,
    rows: List<DayExerciseWithInfo>,
    onOpenExercise: (String) -> Unit,
    onRemove: (Long) -> Unit,
    onEdit: (DayExerciseWithInfo) -> Unit,
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
        DayExerciseRow(
            item,
            onClick = { onOpenExercise(item.exercise.id) },
            onEdit = { onEdit(item) },
            onRemove = { onRemove(item.dxId) }
        )
    }
}

@Composable
private fun DayExerciseRow(
    item: DayExerciseWithInfo,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val muscle = MuscleGroup.fromName(item.exercise.muscleGroup)
    GlassCard(Modifier.fillMaxWidth(), cornerRadius = 18.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExerciseThumb(
                clipAsset = item.exercise.clipAsset,
                accent = muscle.accent,
                contentDescription = item.exercise.name,
                size = 40.dp,
                cornerRadius = 12.dp
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.exercise.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    if (item.exercise.durationSeconds > 0) "${item.dxSets} sets x ${item.effectiveDuration}s"
                    else "${item.dxSets} sets x ${item.dxReps}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Customize", tint = TextSecondary)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = TextSecondary)
            }
        }
    }
}
