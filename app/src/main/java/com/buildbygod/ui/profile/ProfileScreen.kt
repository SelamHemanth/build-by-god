package com.buildbygod.ui.profile

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.ActivityLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.Sex
import com.buildbygod.domain.model.Units
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentPink
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.Pill
import com.buildbygod.ui.theme.SectionHeader
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

private enum class EditField(val title: String, val unit: String) {
    NAME("Your name", ""),
    HEIGHT("Height", "cm"),
    WEIGHT("Weight", "kg"),
    AGE("Age", "years")
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile
    val tokens = LocalFitTokens.current
    var editing by remember { mutableStateOf<EditField?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profile", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), onClick = { editing = EditField.NAME }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(72.dp).clip(CircleShape).background(tokens.accentGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            profile.name.take(1).ifBlank { "A" }.uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = Ink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(profile.name.ifBlank { "Athlete" }, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                        Text(profile.goal.label, style = MaterialTheme.typography.bodyMedium, color = tokens.accent)
                        Text("Tap to edit name", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }
        }

        // ---- Workout stats ----
        item { SectionHeader("Your stats") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Icons.Filled.CheckCircle, AccentGreen, "${state.stats.totalWorkouts}", "Workouts", Modifier.weight(1f))
                StatTile(Icons.Filled.LocalFireDepartment, AccentAmber, "${state.stats.streak}", "Day streak", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Icons.Filled.Timer, AccentBlue, "${state.stats.totalMinutes}", "Total minutes", Modifier.weight(1f))
                StatTile(Icons.Filled.Bolt, AccentPink, "${state.stats.totalCaloriesBurned}", "Calories burned", Modifier.weight(1f))
            }
        }

        // ---- Body details ----
        item { SectionHeader("Body details") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailTile("Height", if (profile.heightCm > 0) "${profile.heightCm} cm" else "Set", Modifier.weight(1f)) { editing = EditField.HEIGHT }
                DetailTile("Weight", if (currentWeight(profile.weightKg, profile.startWeight) > 0) "${currentWeight(profile.weightKg, profile.startWeight)} kg" else "Set", Modifier.weight(1f)) { editing = EditField.WEIGHT }
                DetailTile("Age", if (profile.age > 0) "${profile.age}" else "Set", Modifier.weight(1f)) { editing = EditField.AGE }
            }
        }
        item {
            Text("Sex", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sex.entries.forEach { Pill(it.label, profile.sex == it, { vm.setSex(it) }) }
            }
        }
        item {
            Text("Activity level", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.entries.forEach { Pill(it.label, profile.activityLevel == it, { vm.setActivity(it) }) }
            }
        }

        // ---- Nutrition ----
        item { SectionHeader("Daily nutrition target") }
        item {
            val n = state.nutrition
            if (n == null) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("Complete your body details", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        "Add your height, weight and age to get personalized calorie and protein targets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Calorie target", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text("${n.calorieTarget} kcal", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("BMR ${n.bmr}  -  TDEE ${n.tdee} kcal", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroPill("Protein", "${n.proteinG} g", AccentPink, Modifier.weight(1f))
                        MacroPill("Carbs", "${n.carbsG} g", AccentBlue, Modifier.weight(1f))
                        MacroPill("Fat", "${n.fatG} g", AccentAmber, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    MacroPill("Water", "${n.waterMl} ml / day", AccentGreen, Modifier.fillMaxWidth())
                }
            }
        }

        // ---- Goal & units ----
        item { SectionHeader("Goal") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Goal.entries.forEach { Pill(it.label, profile.goal == it, { vm.setGoal(it) }) }
            }
        }
        item { SectionHeader("Units") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Units.entries.forEach { Pill(it.label, profile.units == it, { vm.setUnits(it) }) }
            }
        }

        item { SectionHeader("Default reminder lead time") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 15, 30, 60).forEach { min ->
                    Pill(if (min == 0) "On time" else "$min min before", profile.defaultReminderLead == min, { vm.setReminderLead(min) })
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }) {
                Text("Enable workout notifications", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Allow BuildByGod to remind you when it's time to train.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        item {
            Text("BuildByGod v1.0  -  All data stays on your device.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(90.dp))
        }
    }

    editing?.let { field ->
        val initial = when (field) {
            EditField.NAME -> profile.name
            EditField.HEIGHT -> if (profile.heightCm > 0) profile.heightCm.toString() else ""
            EditField.WEIGHT -> currentWeight(profile.weightKg, profile.startWeight).let { if (it > 0) it.toString() else "" }
            EditField.AGE -> if (profile.age > 0) profile.age.toString() else ""
        }
        EditDialog(
            field = field,
            initial = initial,
            onDismiss = { editing = null },
            onConfirm = { value ->
                when (field) {
                    EditField.NAME -> vm.setName(value)
                    EditField.HEIGHT -> value.toIntOrNull()?.let { vm.setHeight(it) }
                    EditField.WEIGHT -> value.toFloatOrNull()?.let { vm.setWeight(it) }
                    EditField.AGE -> value.toIntOrNull()?.let { vm.setAge(it) }
                }
                editing = null
            }
        )
    }
}

private fun currentWeight(weightKg: Float, startWeight: Float): Float =
    if (weightKg > 0f) weightKg else startWeight

@Composable
private fun StatTile(
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

@Composable
private fun DetailTile(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier, cornerRadius = 18.dp, onClick = onClick) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MacroPill(
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface2.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = tint, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EditDialog(
    field: EditField,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    val tokens = LocalFitTokens.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = tokens.sheet,
        title = { Text(field.title, color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                suffix = { if (field.unit.isNotEmpty()) Text(field.unit, color = TextSecondary) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (field == EditField.NAME) KeyboardType.Text else KeyboardType.Number
                )
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Save", color = tokens.accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}
