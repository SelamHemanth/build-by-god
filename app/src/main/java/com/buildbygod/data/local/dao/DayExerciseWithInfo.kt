package com.buildbygod.data.local.dao

import androidx.room.Embedded
import com.buildbygod.data.local.entity.ExerciseEntity

/** Join result: an exercise plus the day-specific prescription. */
data class DayExerciseWithInfo(
    @Embedded val exercise: ExerciseEntity,
    val dxId: Long,
    val dxSection: String,
    val dxOrder: Int,
    val dxSets: Int,
    val dxReps: String,
    val dxDuration: Int = -1
) {
    /** Duration to actually use: the per-day override if set, otherwise the exercise default. */
    val effectiveDuration: Int get() = if (dxDuration >= 0) dxDuration else exercise.durationSeconds
}
