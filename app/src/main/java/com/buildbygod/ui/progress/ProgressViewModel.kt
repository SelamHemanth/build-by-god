package com.buildbygod.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.entity.SessionLogEntity
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.ProgressRepository
import com.buildbygod.domain.model.BodyComposition
import com.buildbygod.domain.model.BodyMetrics
import com.buildbygod.domain.model.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** Selectable look-back windows for the progress stats. */
enum class ProgressRange(val days: Int, val label: String) {
    WEEK(7, "7 days"),
    MONTH(30, "30 days"),
    QUARTER(90, "90 days"),
    YEAR(365, "1 year")
}

data class ProgressUiState(
    val range: ProgressRange = ProgressRange.MONTH,
    val sessions: List<SessionLogEntity> = emptyList(),
    val activeDays: Set<Long> = emptySet(),
    // overall
    val streak: Int = 0,
    val total: Int = 0,
    // scaled to the selected range
    val rangeWorkouts: Int = 0,
    val rangeActiveDays: Int = 0,
    val rangeMinutes: Int = 0,
    val consistency: Float = 0f,
    // calendar
    val month: YearMonth = YearMonth.now(),
    // profile-derived body insights
    val body: BodyComposition? = null,
    val weightUnit: WeightUnit = WeightUnit.KG
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    repo: ProgressRepository,
    profileRepo: ProfileRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()
    private val range = MutableStateFlow(ProgressRange.MONTH)
    private val month = MutableStateFlow(YearMonth.now())

    val state = combine(
        repo.sessions(),
        repo.sessionDays(),
        range,
        month,
        profileRepo.profile
    ) { sessions, days, range, month, profile ->
        val activeSet = days.toSet()
        val windowStart = today - (range.days - 1)
        val inRange = sessions.filter { it.epochDay in windowStart..today }
        val activeInRange = activeSet.count { it in windowStart..today }

        val weight = if (profile.weightKg > 0f) profile.weightKg else profile.startWeight
        val body = if (BodyMetrics.isComplete(weight, profile.heightCm, profile.age)) {
            BodyMetrics.compute(
                sex = profile.sex,
                weightKg = weight,
                heightCm = profile.heightCm,
                age = profile.age,
                goal = profile.primaryGoal,
                startWeightKg = profile.startWeight
            )
        } else null

        ProgressUiState(
            range = range,
            sessions = sessions,
            activeDays = activeSet,
            streak = computeStreak(activeSet),
            total = sessions.size,
            rangeWorkouts = inRange.size,
            rangeActiveDays = activeInRange,
            rangeMinutes = (inRange.sumOf { it.durationSeconds } / 60).toInt(),
            consistency = (activeInRange.toFloat() / range.days).coerceIn(0f, 1f),
            month = month,
            body = body,
            weightUnit = profile.weightUnit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun setRange(r: ProgressRange) = range.update { r }

    fun previousMonth() = month.update { it.minusMonths(1) }
    fun nextMonth() = month.update { if (it < YearMonth.now()) it.plusMonths(1) else it }

    private fun computeStreak(days: Set<Long>): Int {
        var streak = 0
        var cursor = today
        if (!days.contains(cursor)) cursor -= 1
        while (days.contains(cursor)) {
            streak += 1
            cursor -= 1
        }
        return streak
    }
}
