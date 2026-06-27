package com.buildbygod.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Links an exercise to a day, with day-specific ordering and prescription. */
@Entity(
    tableName = "day_exercises",
    indices = [Index("dayOfWeek"), Index("exerciseId")]
)
data class DayExerciseCrossRef(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,
    val exerciseId: String,
    /** ExerciseType name to pin the section it appears in for this day. */
    val section: String,
    val orderIndex: Int,
    val sets: Int,
    val reps: String
)
