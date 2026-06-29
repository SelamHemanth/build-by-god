package com.buildbygod.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.datastore.DietStore
import com.buildbygod.data.local.entity.SessionLogEntity
import com.buildbygod.data.repository.IntakeRepository
import com.buildbygod.data.repository.IntakeTotals
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.ProgressRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.domain.model.BodyComposition
import com.buildbygod.domain.model.BodyMetrics
import com.buildbygod.domain.model.FoodCategory
import com.buildbygod.domain.model.NutritionCalculator
import com.buildbygod.domain.model.NutritionPlan
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

/** Ordering options for the logged-sessions list. */
enum class SessionSort(val label: String) {
    RECENT("Most recent"),
    OLDEST("Oldest first"),
    COMPLETION("Highest completion"),
    LONGEST("Longest")
}

data class ProgressUiState(
    val range: ProgressRange = ProgressRange.MONTH,
    val sessions: List<SessionLogEntity> = emptyList(),
    val sessionSort: SessionSort = SessionSort.RECENT,
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
    /** Days-of-week (1=Mon..7=Sun) that have a planned (non-rest) workout. */
    val plannedDows: Set<Int> = emptySet(),
    /** Epoch days the user marked their diet on track. */
    val dietDays: Set<Long> = emptySet(),
    val dietStreak: Int = 0,
    val rangeDietDays: Int = 0,
    // profile-derived body insights
    val body: BodyComposition? = null,
    val nutrition: NutritionPlan? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    // today's actual food + supplement intake (logged on the Diet screen)
    val consumedToday: IntakeTotals = IntakeTotals(),
    val loggedToday: Int = 0,
    /** Names of supplements the user logged today. */
    val supplementsToday: List<String> = emptyList()
)

private data class ProgressInputs(
    val sessions: List<SessionLogEntity>,
    val activeDays: Set<Long>,
    val profile: com.buildbygod.data.datastore.UserProfile,
    val plannedDows: Set<Int>,
    val dietDays: Set<Long>
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    repo: ProgressRepository,
    profileRepo: ProfileRepository,
    workoutRepo: WorkoutRepository,
    dietStore: DietStore,
    intakeRepo: IntakeRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()
    private val range = MutableStateFlow(ProgressRange.MONTH)
    private val month = MutableStateFlow(YearMonth.now())
    private val sessionSort = MutableStateFlow(SessionSort.RECENT)
    private val intakeToday = intakeRepo.observeForDay(today)

    private val inputs = combine(
        repo.sessions(),
        repo.sessionDays(),
        profileRepo.profile,
        workoutRepo.days(),
        dietStore.prefs
    ) { sessions, days, profile, workoutDays, dietPrefs ->
        ProgressInputs(
            sessions = sessions,
            activeDays = days.toSet(),
            profile = profile,
            plannedDows = workoutDays.filter { !it.isRestDay }.map { it.dayOfWeek }.toSet(),
            dietDays = dietPrefs.followedDays
        )
    }

    val state = combine(inputs, range, month, intakeToday, sessionSort) { input, range, month, eaten, sort ->
        val sessions = when (sort) {
            SessionSort.RECENT -> input.sessions
            SessionSort.OLDEST -> input.sessions.sortedWith(compareBy({ it.epochDay }, { it.id }))
            SessionSort.COMPLETION -> input.sessions.sortedByDescending {
                if (it.totalCount == 0) 0f else it.completedCount.toFloat() / it.totalCount
            }
            SessionSort.LONGEST -> input.sessions.sortedByDescending { it.durationSeconds }
        }
        val activeSet = input.activeDays
        val profile = input.profile
        val windowStart = today - (range.days - 1)
        val inRange = sessions.filter { it.epochDay in windowStart..today }
        val activeInRange = activeSet.count { it in windowStart..today }
        val dietInRange = input.dietDays.count { it in windowStart..today }

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

        val nutrition = if (NutritionCalculator.isComplete(weight, profile.heightCm, profile.age)) {
            NutritionCalculator.compute(
                profile.sex, weight, profile.heightCm, profile.age,
                profile.activityLevel, profile.primaryGoal
            )
        } else null

        ProgressUiState(
            range = range,
            sessions = sessions,
            sessionSort = sort,
            activeDays = activeSet,
            streak = computeStreak(activeSet),
            total = sessions.size,
            rangeWorkouts = inRange.size,
            rangeActiveDays = activeInRange,
            rangeMinutes = (inRange.sumOf { it.durationSeconds } / 60).toInt(),
            consistency = (activeInRange.toFloat() / range.days).coerceIn(0f, 1f),
            month = month,
            plannedDows = input.plannedDows,
            dietDays = input.dietDays,
            dietStreak = computeStreak(input.dietDays),
            rangeDietDays = dietInRange,
            body = body,
            nutrition = nutrition,
            weightUnit = profile.weightUnit,
            consumedToday = IntakeRepository.totals(eaten),
            loggedToday = eaten.size,
            supplementsToday = eaten
                .filter { it.food.category == FoodCategory.SUPPLEMENT }
                .map { it.food.name }
                .distinct()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun setRange(r: ProgressRange) = range.update { r }
    fun setSessionSort(s: SessionSort) = sessionSort.update { s }

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
