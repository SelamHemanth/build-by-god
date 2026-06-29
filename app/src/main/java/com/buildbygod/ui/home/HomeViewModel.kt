package com.buildbygod.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.datastore.ActiveSession
import com.buildbygod.data.datastore.ActiveSessionStore
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.ProgressRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val name: String = "",
    val photoUri: String? = null,
    val profileFrame: Int = 0,
    val todayDow: Int = LocalDate.now().dayOfWeek.value,
    val today: WorkoutDayEntity? = null,
    val warmups: Int = 0,
    val mains: Int = 0,
    val stretches: Int = 0,
    val week: List<WorkoutDayEntity> = emptyList(),
    val completedToday: Boolean = false,
    val streak: Int = 0,
    val totalSessions: Int = 0,
    /** Name derived from today's actual exercises (e.g. "Chest & Back"), for the rename prefill. */
    val suggestedName: String = "",
    /** A workout in progress the user can resume, if any. */
    val activeSession: ActiveSession? = null
) {
    val total: Int get() = warmups + mains + stretches
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepo: ProfileRepository,
    private val workoutRepo: WorkoutRepository,
    progressRepo: ProgressRepository,
    activeSessionStore: ActiveSessionStore
) : ViewModel() {

    private val todayDow = LocalDate.now().dayOfWeek.value
    private val todayEpoch = LocalDate.now().toEpochDay()

    private val todayExercises = workoutRepo.dayExercises(todayDow)

    val state = combine(
        profileRepo.profile,
        workoutRepo.days(),
        todayExercises,
        progressRepo.sessionDays(),
        activeSessionStore.session
    ) { profile, days, todayList, sessionDays, active ->
        val today = days.firstOrNull { it.dayOfWeek == todayDow }
        HomeUiState(
            suggestedName = deriveDayName(todayList),
            name = profile.name,
            photoUri = profile.photoUri,
            profileFrame = profile.profileFrame,
            todayDow = todayDow,
            today = today,
            warmups = todayList.count { it.dxSection == ExerciseType.WARMUP.name },
            mains = todayList.count { it.dxSection == ExerciseType.MAIN.name },
            stretches = todayList.count { it.dxSection == ExerciseType.STRETCH.name },
            week = days,
            completedToday = sessionDays.contains(todayEpoch),
            streak = computeStreak(sessionDays.toSet()),
            totalSessions = sessionDays.size,
            activeSession = active.takeIf { it.active }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** Persist a new name for today's workout day. */
    fun renameToday(name: String) {
        val today = state.value.today ?: return
        val clean = name.trim()
        if (clean.isBlank()) return
        viewModelScope.launch { workoutRepo.saveDay(today.copy(title = clean)) }
    }

    /** Builds a focus name from the muscle groups actually trained today. */
    private fun deriveDayName(todayList: List<com.buildbygod.data.local.dao.DayExerciseWithInfo>): String {
        val mains = todayList.filter { it.dxSection == ExerciseType.MAIN.name }
        if (mains.isEmpty()) return ""
        val groups = mains
            .map { MuscleGroup.fromName(it.exercise.muscleGroup) }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key.label }
        return when (groups.size) {
            0 -> ""
            1 -> "${groups[0]} Day"
            else -> "${groups[0]} & ${groups[1]}"
        }
    }

    private fun computeStreak(days: Set<Long>): Int {
        var streak = 0
        var cursor = todayEpoch
        // Allow today to be incomplete without breaking the streak.
        if (!days.contains(cursor)) cursor -= 1
        while (days.contains(cursor)) {
            streak += 1
            cursor -= 1
        }
        return streak
    }
}
