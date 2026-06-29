package com.buildbygod.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.HeightUnit
import com.buildbygod.domain.model.Sex
import com.buildbygod.domain.model.UnitConvert
import com.buildbygod.domain.model.WeightUnit
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.Pill
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val MS_PER_DAY = 86_400_000L
private val dobFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SexChips(selected: Sex, onSelect: (Sex) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Sex.entries.forEach { Pill(it.label, selected == it, { onSelect(it) }) }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GoalChips(selected: Set<Goal>, onToggle: (Goal) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Goal.entries.forEach { Pill(it.label, it in selected, { onToggle(it) }) }
    }
}

@Composable
fun ExperiencePicker(selected: ExperienceLevel, onSelect: (ExperienceLevel) -> Unit) {
    val tokens = LocalFitTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExperienceLevel.entries.forEach { level ->
            val isSel = level == selected
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSel) tokens.accent.copy(alpha = 0.16f) else Surface2.copy(alpha = 0.5f))
                    .clickable { onSelect(level) }
                    .padding(14.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${level.label}  ·  ${level.duration}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSel) tokens.accent else TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(level.blurb, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
                if (isSel) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = tokens.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumField(value: String, suffix: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        singleLine = true,
        suffix = { Text(suffix, color = TextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = Surface2,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        modifier = modifier
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HeightPicker(heightCm: Int, unit: HeightUnit, onHeightCm: (Int) -> Unit, onUnit: (HeightUnit) -> Unit) {
    Column {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeightUnit.entries.forEach { Pill(it.label, unit == it, { onUnit(it) }) }
        }
        Spacer(Modifier.height(10.dp))
        when (unit) {
            HeightUnit.CM -> {
                var t by remember(unit) { mutableStateOf(if (heightCm > 0) heightCm.toString() else "") }
                NumField(t, "cm", Modifier.fillMaxWidth()) { t = it; onHeightCm(t.toIntOrNull() ?: 0) }
            }
            HeightUnit.IN -> {
                var t by remember(unit) {
                    mutableStateOf(if (heightCm > 0) Math.round(UnitConvert.cmToInches(heightCm)).toString() else "")
                }
                NumField(t, "in", Modifier.fillMaxWidth()) { t = it; onHeightCm(UnitConvert.inchesToCm(t.toDoubleOrNull() ?: 0.0)) }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun WeightPicker(weightKg: Float, unit: WeightUnit, onWeightKg: (Float) -> Unit, onUnit: (WeightUnit) -> Unit) {
    Column {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeightUnit.entries.forEach { Pill(it.suffix, unit == it, { onUnit(it) }) }
        }
        Spacer(Modifier.height(10.dp))
        var t by remember(unit) {
            mutableStateOf(
                if (weightKg > 0f) when (unit) {
                    WeightUnit.KG -> trim(weightKg.toDouble())
                    WeightUnit.LB -> trim(UnitConvert.kgToLb(weightKg))
                    WeightUnit.ST -> trim(UnitConvert.kgToSt(weightKg))
                } else ""
            )
        }
        NumField(t, unit.suffix, Modifier.fillMaxWidth()) {
            t = it
            val v = it.toDoubleOrNull() ?: 0.0
            onWeightKg(
                when (unit) {
                    WeightUnit.KG -> v.toFloat()
                    WeightUnit.LB -> UnitConvert.lbToKg(v)
                    WeightUnit.ST -> UnitConvert.stToKg(v)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DobField(dobEpochDay: Long, age: Int, onPick: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val tokens = LocalFitTokens.current

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface2.copy(alpha = 0.5f))
            .clickable { open = true }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Date of birth", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                if (dobEpochDay > 0) LocalDate.ofEpochDay(dobEpochDay).format(dobFormatter) else "Tap to select",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (dobEpochDay > 0) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("Age", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("$age", style = MaterialTheme.typography.titleMedium, color = tokens.accent, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (open) {
        val initial = if (dobEpochDay > 0) dobEpochDay * MS_PER_DAY
        else LocalDate.now().minusYears(20).toEpochDay() * MS_PER_DAY
        val todayMs = LocalDate.now().toEpochDay() * MS_PER_DAY
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initial,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
            }
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        val day = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                        onPick(day)
                    }
                    open = false
                }) { Text("OK", color = tokens.accent) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel", color = TextSecondary) } }
        ) {
            DatePicker(state = pickerState, showModeToggle = true)
        }
    }
}

private fun trim(v: Double): String {
    val r = Math.round(v * 10.0) / 10.0
    return if (r % 1.0 == 0.0) r.toInt().toString() else r.toString()
}
