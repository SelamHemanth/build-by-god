package com.buildbygod.ui.daydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.dao.DayExerciseWithInfo
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.data.repository.ExerciseRepository
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.domain.model.Difficulty
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.ExperienceLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayDetailUiState(
    val day: WorkoutDayEntity? = null,
    val warmups: List<DayExerciseWithInfo> = emptyList(),
    val mains: List<DayExerciseWithInfo> = emptyList(),
    val stretches: List<DayExerciseWithInfo> = emptyList()
) {
    val total: Int get() = warmups.size + mains.size + stretches.size
}

/** One option in the add-exercise picker, flagged when it matches the user's level. */
data class PickerItem(val exercise: ExerciseEntity, val suggested: Boolean)

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    private val profileRepo: ProfileRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    val day: Int = savedState["day"] ?: 1

    val state = combine(
        workoutRepo.day(day),
        workoutRepo.dayExercises(day)
    ) { dayEntity, list ->
        DayDetailUiState(
            day = dayEntity,
            warmups = list.filter { it.dxSection == ExerciseType.WARMUP.name },
            mains = list.filter { it.dxSection == ExerciseType.MAIN.name },
            stretches = list.filter { it.dxSection == ExerciseType.STRETCH.name }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayDetailUiState())

    private fun targetDifficulty(exp: ExperienceLevel): Difficulty = when (exp) {
        ExperienceLevel.NEW, ExperienceLevel.BEGINNER -> Difficulty.BEGINNER
        ExperienceLevel.INTERMEDIATE -> Difficulty.INTERMEDIATE
        ExperienceLevel.ADVANCED, ExperienceLevel.ELITE -> Difficulty.ADVANCED
    }

    /**
     * Exercises to add for the section, ranked so the user's level shows first and flagged as
     * "suggested" when the difficulty matches their experience and the day's focus.
     */
    fun suggestedPickerFor(section: ExerciseType): Flow<List<PickerItem>> =
        combine(exerciseRepo.byType(section.name), workoutRepo.day(day), profileRepo.profile) { all, dayEntity, profile ->
            val target = targetDifficulty(profile.experience)
            val focusGroups = dayEntity?.focus.orEmpty().lowercase()
            all.sortedWith(
                compareBy(
                    { kotlin.math.abs(Difficulty.fromName(it.difficulty).rank - target.rank) },
                    { if (focusGroups.contains(it.muscleGroup.lowercase())) 0 else 1 },
                    { it.name }
                )
            ).map { ex ->
                PickerItem(ex, suggested = Difficulty.fromName(ex.difficulty) == target)
            }
        }

    fun addExercise(ex: ExerciseEntity, section: ExerciseType, sets: Int, reps: String, durationSeconds: Int) {
        viewModelScope.launch {
            workoutRepo.addExerciseToDay(day, ex.id, section.name, sets, reps, durationSeconds)
        }
    }

    fun updatePrescription(dxId: Long, sets: Int, reps: String, durationSeconds: Int) {
        viewModelScope.launch { workoutRepo.updatePrescription(dxId, sets, reps, durationSeconds) }
    }

    fun remove(dxId: Long) {
        viewModelScope.launch { workoutRepo.removeDayExercise(dxId) }
    }
}
